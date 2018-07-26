package pathwitch

import scala.util.matching.Regex
import pathwitch.Glob.Separator

/**
  * Configuration for glob matcher.
  *
  * @param separator    Path separator character
  * @param unixStyle    Force unix style path separator ("/")
  * @param convertPath  Convert path character when building regex patterns
  */
case class GlobConfig(separator: Separator,
                      unixStyle: Boolean = false,
                      convertPath: Boolean = false)

/**
  * Basic functionality of glob.
  */
trait GlobLike extends (String => Boolean) {
  /**
    * Test `input` with this glob-like pattern.
    * @param input string
    * @return true if this pattern matched, else false
    */
  def matches(input: String): Boolean

  /**
    * Apply test over `values` collection.
    * @param values collection to apply matcher function
    * @return filtered collection
    */
  def filter(values: Seq[String]): Seq[String] =
    values.filter(this)
  def filter(values: Iterable[String]): Iterable[String] =
    values.filter(this)
  def filter(iterator: Iterator[String]): Iterator[String] =
    iterator.filter(this)

  /**
    * Apply this predicate function.
    * @param input string
    * @return true if this pattern matched, else false
    */
  def apply(input: String): Boolean = this.matches(input)
}

/**
  * Set of glob rules to easily test multiple patterns.
  *
  * @param rules list of globs, it is a [[Seq]] just because to preserve orders
  */
case class GlobSet private[pathwitch] (rules: Seq[Glob]) extends GlobLike {
  /**
    * Tests if any of `rules` matches with given `input`.
    * @param input string
    * @return [[Boolean]]
    */
  def matches(input: String): Boolean = {
    rules.exists(glob => glob(input))
  }

  /**
    * Tests that no pattern in `rules` are matching `input`.
    * @param input string
    * @return true iff all of patterns are not matched
    */
  def noMatch(input: String): Boolean =
    rules.forall(glob => !glob(input))

  /**
    * Ignore all occurrence in `values` collection that matches any of this `rules`.
    * @param values collection
    * @return new collection which all elements matches some pattern are rejected
    */
  def ignoreAllIn(values: Seq[String]): Seq[String] =
    values.filter(this.noMatch)
  def ignoreAllIn(values: Iterable[String]): Iterable[String] =
    values.filter(this.noMatch)
  def ignoreAllIn(values: Iterator[String]): Iterator[String] =
    values.filter(this.noMatch)

  /**
    * Convert rules to [[Set]].
    * @return
    */
  def toSet: Set[Glob] = rules.toSet

  override def toString(): String =
    rules.mkString("GlobSet(", ",", ")")
}

/**
  * GlobSet companion.
  */
object GlobSet {
  /**
    * Create new [[GlobSet]].
    *
    * @param patterns collection of patterns, automatically removed duplication
    * @param config glob configuration
    * @return [[GlobSet]] object
    */
  def apply(patterns: Iterable[String], config: GlobConfig): GlobSet =
    new GlobSet(patterns.toList.distinct.map(p => new Glob(p, config)))

  def apply(iterator: Iterator[String], config: GlobConfig): GlobSet =
    new GlobSet(iterator.toList.distinct.map(p => new Glob(p, config)))
}

/**
  * Immutable glob pattern, backed up by Java RegExp syntax.
  * @param pattern string
  * @param config configuration
  */
case class Glob(pattern: String, config: GlobConfig) extends GlobLike {
  /**
    * Underlying Regex object.
    */
  val regex: Regex = Converter.globToRegex(pattern, config).r

  /**
    * Shortcut to `regex.findFirstIn`.
    * @param input string
    * @return
    */
  def find(input: String): Option[String] =
    regex.findFirstIn(input)

  /**
    * Tests if path `input` matches when the `base` prefix removed from `input`
    * @param input string
    * @param prefix  base path
    * @return
    */
  def matchesIn(input: String, prefix: String): Boolean = {
    val that = if (config.unixStyle) input.replaceAll("\\\\", "/") else input
    find(that).contains(that stripPrefix prefix)
  }

  /**
    * Returns true if `this` pattern matches with `input`.
    * @param input string
    * @return
    */
  def matches(input: String): Boolean = {
    val that = if (config.unixStyle) input.replaceAll("\\\\", "/") else input
    find(that).nonEmpty
  }

  /**
    * Create new [[Glob]] object that shares pattern string with `this` but
    * uses specified configuration.
    *
    * @param newConfig configuration
    * @return
    */
  def withConfig(newConfig: GlobConfig): Glob =
    new Glob(pattern, newConfig)

  override def toString(): String =
    "Glob(" + pattern + "," + regex.toString + ")"
}

/**
  * Glob companion.
  */
object Glob {

  /**
    * Create new glob object.
    *
    * @param pattern string
    * @param unixStyle boolean
    * @param convertPath boolean
    * @param separator [[Glob.Slash]] or [[Glob.Backslash]]
    * @return
    */
  def apply(pattern: String,
            unixStyle: Boolean = false,
            convertPath: Boolean = false
           )(implicit separator: Separator): Glob =
    new Glob(pattern, GlobConfig(separator, unixStyle))

  /**
    * Create new glob set.
    *
    * @param patterns collection of pattern strings
    * @param unixStyle boolean
    * @param convertPath boolean
    * @param separator [[Glob.Slash]] or [[Glob.Backslash]]
    * @return
    */
  def globSet(patterns: Iterable[String],
              unixStyle: Boolean = false,
              convertPath: Boolean = false
             )(implicit separator: Separator): GlobSet = {
    val config = GlobConfig(separator, unixStyle)
    GlobSet(patterns, config)
  }

  /**
    * Path separator enum type.
    * @param char path separator
    * @param regexString regex pattern string
    */
  sealed abstract class Separator(val char: Char, val regexString: String)

  /**
    * File path separator in Unix-like platform ("/").
    */
  case object Slash extends Separator('/', "/")

  /**
    * File path separator in Windows platform ("\").
    */
  case object Backslash extends Separator('\\', "\\\\")

  def separatorFromChar(char: Char): Separator = char match {
    case '/'  => Glob.Slash
    case '\\' => Glob.Backslash
    case _    => throw new IllegalArgumentException(
      s"'$char' is not a valid path separator"
    )
  }
}