package pathwitch

import scala.util.matching.Regex
import pathwitch.Glob.Separator

/**
  * Configuration for glob matcher.
  *
  * @param separator
  * @param forceSlash
  * @param addSlash
  */
case class GlobConfig(
  /** Path separator character */
  separator: Separator,
  /** Whether to convert all separator character in input to '/' when matching */
  forceSlash: Boolean = false,
  /** Whether to prepend separator character when pattern starts with '*' */
  addSlash: Boolean = false
)

/**
  * Basic functionality of glob.
  */
trait GlobLike extends (String => Boolean) {
  /**
    * Test `input` with this glob-like pattern.
    * @param input
    * @return
    */
  def matches(input: String): Boolean

  /**
    * Apply test over `values` collection.
    * @param values
    * @return
    */
  def filter(values: Seq[String]): Seq[String] =
    values.filter(this)
  def filter(values: Iterable[String]): Iterable[String] =
    values.filter(this)
  def filter(iterator: Iterator[String]): Iterator[String] =
    iterator.filter(this)

  /**
    * Apply this predicate function.
    * @param input
    * @return
    */
  def apply(input: String): Boolean = this.matches(input)
}

/**
  *
  * @param rules
  */
case class GlobSet private[pathwitch] (rules: Seq[Glob]) extends GlobLike {
  /**
    * Tests if any of `rules` matches with given `input`.
    * @param input
    * @return
    */
  def matches(input: String): Boolean = {
    rules.exists(glob => glob(input))
  }

  /**
    * Tests that no pattern in `rules` are matching `input`.
    * @param input
    * @return
    */
  def noMatch(input: String): Boolean =
    rules.forall(glob => !glob(input))

  /**
    * Ignore all occurrence in `values` collection that matches any of this `rules`.
    * @param values
    * @return
    */
  def ignore(values: Seq[String]): Seq[String] =
    values.filter(this.noMatch)
  def ignore(values: Iterable[String]): Iterable[String] =
    values.filter(this.noMatch)
  def ignore(values: Iterator[String]): Iterator[String] =
    values.filter(this.noMatch)

  override def toString(): String =
    rules.mkString("GlobSet(", ",", ")")
}

/**
  * Immutable glob pattern, backed up by Java RegExp syntax.
  * @param pattern
  */
case class Glob(pattern: String, config: GlobConfig) extends GlobLike {
  /**
    * Underlying Regex object.
    */
  val regex: Regex = Converter.globToRegex(pattern, config).r

  /**
    * Returns true if `this` pattern matches with `input`.
    * @param input
    * @return
    */
  def matches(input: String): Boolean = {
    val source0 = if (config.forceSlash) input.replaceAll("\\\\", "/") else input
    regex.findFirstIn(source0).nonEmpty
  }

  override def toString(): String =
    "Glob(" + pattern + "," + regex.toString + ")"
}

/**
  * Glob companion.
  */
object Glob {

  def apply(pattern: String, forceSlash: Boolean = false,
            addSlash: Boolean = false)(implicit separator: Separator): Glob =
    new Glob(pattern, GlobConfig(separator, forceSlash, addSlash))

  /**
    * Create new glob set.
    * @param patterns
    * @return
    */
  def globSet(patterns: Iterable[String])(implicit separator: Separator): GlobSet = {
    val config = GlobConfig(separator)
    new GlobSet(patterns.toList.map(p => new Glob(p, config)))
  }

  /**
    * Path separator enum type.
    * @param char
    */
  sealed abstract class Separator(val char: Char)

  /**
    * File path separator in Unix-like platform ("/").
    */
  case object Slash extends Separator('/')

  /**
    * File path separator in Windows platform ("\").
    */
  case object Backslash extends Separator('\\')
}