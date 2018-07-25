package pathwitch

import java.io.File
import pathwitch.Glob.Separator

object ImplicitSeparator {
  implicit val value: Separator =
    Glob.separatorFromChar(File.separatorChar)
}
