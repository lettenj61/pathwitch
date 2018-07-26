package pathwitch

import better.files.File
import better.files.FileOps
import utest._
import pathwitch.Implicits.pathSeparator

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
      def assertFiles(syntax: String, unixStyle: Boolean = true) = {
        val expected = src.glob(syntax).toList.map(_.toString).sortBy(s => s)
        // Work around for windows tests
        val normalizedSyntax = (src.toString.replaceAll("\\\\", "/")) + "/" + syntax
        val glob = Glob(normalizedSyntax, unixStyle = unixStyle)
        val result = glob.filter(answers).sortBy(s => s)
        assert(result == expected)
        result.size
      }
      "relative" - {

      }
      "globStars" - {
        * - assertFiles("library/scala/*.scala")
        * - assertFiles("library/scala/**/*.java")
      }
      "each"- {
        * - assertFiles("**/collection/**")
        * - assertFiles("**/Abstract*12*")
        * - assertFiles("**/*Se[tq].scala")
        * - assertFiles("**.md")
        * - assertFiles("**/*[Uu]t*.*")
      }
      "singleStar" - assertFiles("*")
    }
  }
}
