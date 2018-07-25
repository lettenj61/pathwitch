package pathwitch

import pathwitch.Converter.globToRegex

import utest._

object ConverterTests extends TestSuite {
  implicit val config: GlobConfig = GlobConfig(separator = Glob.Slash)
  def sepCheck(l: String, r: String, sep: Char) = { globToRegex(l, config) ==> r }
  def checkUnix(l: String, r: String) = sepCheck(l, r, '/')
  def checkWindows(l: String, r: String) = sepCheck(l, r, '\\')
  val tests = Tests {
    "Converter" - {
      "star" - {
        "middle" - checkUnix("sc*la", "sc[^/\\\\]*la")
        "escaped" - checkUnix("sc\\*la", "sc\\*la")
      }
      "doubleStar" - {
        "eagerForPath" - checkUnix("src/**/*.js", "src/.*/[^/\\\\]*\\.js")
      }
      "questionMark" - {
        "toDot" - checkUnix("sc?la", "sc.la")
        "escaped" - checkUnix("sc\\?la", "sc\\?la")
      }
      "characterClass" - {
        "preserved" - checkUnix("sc[-abcd]la", "sc[-abcd]la")
        "negated" - checkUnix("sc[!b-z!12]la", "sc[^b-z!12]la")
        "nestedNegation" - checkUnix("sc[[!a-z]!1-9]la", "sc[[^a-z]!1-9]la")
        "escapeFirstCaret" - checkUnix("[^acls]", "[\\^acls]")
      }
      "metaCharacters" - {
        "escaped" - checkUnix(
          "gl?*.()+|^$@%b",
          "gl.[^/\\\\]*\\.\\(\\)\\+\\|\\^\\$\\@\\%b"
        )
        "noEscapeInClass" - checkUnix(
          "gl[?*.()+|^$@%]b",
          "gl[?*.()+|^$@%]b"
        )
      }
      "backslash" - {
        "preserved" - checkUnix("sca\\\\la", "sca\\\\la")
      }
      "slashQ_E" - checkUnix("\\Qscala\\E", "\\\\Qscala\\\\E")
      "group" - {
        "convert" - checkUnix("{java,scala}", "(java|scala)")
        "escapeBraces" - checkUnix("\\{java,scala\\}hello", "\\{java,scala\\}hello")
        "skipCommaEscape" - checkUnix("{java\\,scala}", "(java,scala)")
      }
    }
  }
}
