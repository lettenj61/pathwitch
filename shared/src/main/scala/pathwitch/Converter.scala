package pathwitch

import scala.annotation.switch

/**
  * Glob to Java Regex converter.
  */
object Converter {
  /**
    * Convert glob pattern string into Java RegExp pattern.
    * Based on the code from this StackOverflow question:
    * https://stackoverflow.com/questions/1247772/is-there-an-equivalent-of-java-util-regex-for-glob-type-patterns
    *
    * @param pattern
    * @return
    */
  def globToRegex(pattern: String, config: GlobConfig): String = {
    val trimmed = pattern.trim
    if (trimmed == "*") "[^/\\\\]*"
    else if (trimmed.forall(_ == '*')) ".*"
    else {
      val sb = StringBuilder.newBuilder
      var inClass = 0
      var inGroup = 0
      var classIndex = -1
      var i = 0
      val len = trimmed.length

      while (i < len) {
        val c = trimmed.charAt(i)
        (c: @switch) match {
          case '\\' =>
            i += 1
            if (i >= len) {
              if (config.convertPath) sb.append(config.separator.regexString)
              else sb.append("\\\\")
            } else {
              var next = trimmed.charAt(i)
              (next: @switch) match {
                case ',' => // No escape
                case 'Q' | 'E' => sb.append("""\\""") // Extra escape
                case '\\' if config.convertPath =>
                  sb.append('\\')
                  next = config.separator.char
                case _ => sb.append('\\')
              }
              sb.append(next)
            }
          case '*' =>
            if (inClass == 0) {
              if ((i + 1) >= len) sb.append("[^/\\\\]*")
              else {
                var stars = 1
                var next = trimmed.charAt(i + stars)
                while ((i + stars) < len && next == '*') {
                  stars += 1
                  if (i + stars < len) {
                    next = trimmed.charAt(i + stars)
                  }
                }
                if (stars > 1) {
                  i += (stars - 1)
                  sb.append(".*")
                } else {
                  sb.append("[^/\\\\]*")
                }
              }
            } else {
              sb.append('*')
            }
          case '?' =>
            if (inClass == 0) sb.append('.') else sb.append('?')
          case '[' =>
            inClass += 1
            classIndex = i + 1
            sb.append('[')
          case ']' =>
            inClass -= 1
            sb.append(']')
          case '.' | '(' | ')' | '+' | '|' | '^' | '$' | '@' | '%' =>
            if (inClass == 0 || (classIndex == i && c == '^')) {
              sb.append('\\')
            }
            sb.append(c)
          case '!' =>
            if (classIndex == i) sb.append('^') else sb.append('!')
          case '{' =>
            inGroup += 1
            sb.append('(')
          case '}' =>
            inGroup -= 1
            sb.append(')')
          case ',' =>
            if (inGroup > 0) sb.append('|') else sb.append(',')
          case '/' =>
            if (config.convertPath) sb.append(config.separator.regexString)
            else sb.append('/')
          case _ => sb.append(c)
        }
        i += 1
      }
      sb.result
    }
  }
}
