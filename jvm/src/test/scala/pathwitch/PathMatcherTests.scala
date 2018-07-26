package pathwitch

import better.files.File
import better.files.FileOps
import utest._
import pathwitch.ImplicitSeparator._

object PathMatcherTests extends TestSuite with ProcessHelper {
  val tests = Tests {
    "PathMatcher"- {
      val repo = scala.sys.env.get("PATHWITCH_TEST_PATH").map { path =>
        File(path)
      } getOrElse {
        gitClone("scala", "scala", File.newTemporaryDirectory().toJava).toScala
      }
      val src = repo/"src"
      val answers = src.glob("**").toList.map(_.toString)
      def assertFiles(syntax: String) = {
        val expected = src.glob(syntax).toList.map(_.toString).sortBy(s => s)
        val globbed = Glob(syntax, unixStyle = true).filter(answers).sortBy(s => s)
        assert(globbed == expected)
        (globbed.size, expected.size)
      }
      * - assertFiles("library/scala/*.scala")
      * - assertFiles("**/Abstract*12*")
      * - assertFiles("**/*Se[tq].scala")
      * - assertFiles("**.md")
    }
  }
}
