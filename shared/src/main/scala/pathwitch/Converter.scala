package pathwitch

import scala.annotation.switch

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
    val src0 = pattern.trim
    if (src0.forall(_ == '*')) {
      return ".*"
    }
    val sb = StringBuilder.newBuilder
    var inClass = 0
    var inGroup = 0
    var classIndex = -1
    var i = 0
    val len = src0.length

    // ---- apply configuration
    if (config.prefixed && src0.startsWith("*")) {
      sb.append('/')
    }

    while (i < len) {
      val c = src0.charAt(i)
      (c: @switch) match {
        case '\\' =>
          i += 1
          if (i >= len) sb.append('\\')
          else {
            val next = src0.charAt(i)
            (next: @switch) match {
              case ','        => // No escape
              case 'Q' | 'E'  => sb.append("""\\""") // Extra escape
              case _ => sb.append('\\')
            }
            sb.append(next)
          }
        case '*' =>
          if (inClass == 0) {
            if ((i + 1) >= len) sb.append(".*")
            else {
              var moreStars = 1
              var next = src0.charAt(i + moreStars)
              while ((i + moreStars) < len && next == '*') {
                moreStars += 1
                if (i + moreStars < len) {
                  next = src0.charAt(i + moreStars)
                }
              }
              if (moreStars > 1) {
                i += (moreStars - 1)
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
        case _ => sb.append(c)
      }
      i += 1
    }
    sb.result
  }
}
