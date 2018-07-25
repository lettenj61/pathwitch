package pathwitch

import java.io.File

trait ProcessHelper {
  def gitClone(userName: String, repo: String, dest: File): File = {
    import scala.sys.process._
    if (dest.exists()) dest
    else {
      Seq("git", "clone", s"https://github.com/$userName/$repo.git", dest.toString).!!
      dest
    }
  }
}
