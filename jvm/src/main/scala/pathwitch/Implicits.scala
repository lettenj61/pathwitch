package pathwitch

import java.io.File
import pathwitch.Glob.Separator

object Implicits {
  implicit val pathSeparator: Separator =
    Glob.separatorFromChar(File.separatorChar)
}
