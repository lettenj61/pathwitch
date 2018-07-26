package pathwitch

import pathwitch.Glob.Separator
import utest._

object GlobTests extends TestSuite {
  implicit val separator: Separator = Glob.Slash
  implicit class StringCheck(val lines: String) extends AnyVal {
    def checkMatch(glob: Glob): Unit =
      lines.split("\n").forall(glob)
  }

  def makeGlobSet(patterns: String): GlobSet =
    Glob.globSet(patterns.split("\n"))

  def checkTree(glob: Glob, ref: Seq[String])(implicit tp: framework.TestPath) = {
    val left = glob.filter(fileTree).sortBy(s => s)
    val right = ref.sortBy(s => s)
    assert(left == right)
  }

  // Borrowed from:
  // https://github.com/pathikrit/better-files/blob/bfccb5041239bc5413afade4218ec1fb90d3e3d5/core/src/test/scala/better/files/GlobSpec.scala
  // Skipping links and specials here, as it is string matcher test
  lazy val fileTree =
    """tests
      |tests/tree
      |tests/tree/a
      |tests/tree/a/a2
      |tests/tree/a/a2/a2.txt
      |tests/tree/a/a2/x.txt
      |tests/tree/a/a.not
      |tests/tree/a/a.txt
      |tests/tree/a/x.txt
      |tests/tree/b
      |tests/tree/b/a
      |tests/tree/b/a/ba.txt
      |tests/tree/b/b.txt
      |tests/tree/c
      |tests/tree/c/c.txt
      |tests/tree/c/x.txt
      |tests/tree/empty
      |tests/tree/one.txt
      |tests/tree/readme.md
      |tests/tree/three.txt
      |tests/tree/two.txt
    """.stripMargin.trim.split("\n").toList

  val tests = Tests {
    "Glob" - {
      "matching" - {
        "all" - {
          "doubleStar" - checkTree(Glob("**"), fileTree)
          "prefix" - checkTree(Glob("tree/**"), fileTree.drop(2))
        }
        "single" - {
          Glob("one.txt").filter(fileTree) ==> List("tests/tree/one.txt")
        }
        "partial" - {
          Glob("a/a.txt").filter(fileTree) ==> List("tests/tree/a/a.txt")
        }
        "extension" - checkTree(
          Glob("*.txt"),
          fileTree.filter(_.endsWith(".txt"))
        )
        "subdirectory" - {
          "dirname" - checkTree(
            Glob("**/a/*.txt"),
            List(
              "tests/tree/a/a.txt",
              "tests/tree/a/x.txt",
              "tests/tree/b/a/ba.txt"
            )
          )
          "filename" - checkTree(
            Glob("tree/*/x.txt"),
            List(
              "tests/tree/a/x.txt",
              "tests/tree/c/x.txt"
            )
          )
          "dirGlobWithFileGlob" - checkTree(
            Glob("tree/*/*.txt"),
            List(
              "tests/tree/a/a.txt",
              "tests/tree/a/x.txt",
              "tests/tree/b/b.txt",
              "tests/tree/c/c.txt",
              "tests/tree/c/x.txt"
            )
          )
          "deep" - {
            val expected = List(
              "tests/tree/a/x.txt",
              "tests/tree/a/a2/x.txt",
              "tests/tree/c/x.txt"
            )
            "noPrefix" - checkTree(Glob("**/x.txt"), expected)
            "prefix"- checkTree(Glob("tree/**/x.txt"), expected)
          }
        }
        "parentDirectory" - checkTree(
          Glob("a/*.txt"),
          List(
            "tests/tree/a/a.txt",
            "tests/tree/a/x.txt",
            "tests/tree/b/a/ba.txt"
          )
        )
        "deepFileGlob" - {
          val expected = List(
            "tests/tree/one.txt",
            "tests/tree/two.txt",
            "tests/tree/three.txt",
            "tests/tree/a/a.txt",
            "tests/tree/a/x.txt",
            "tests/tree/a/a2/a2.txt",
            "tests/tree/a/a2/x.txt",
            "tests/tree/b/a/ba.txt",
            "tests/tree/b/b.txt",
            "tests/tree/c/c.txt",
            "tests/tree/c/x.txt"
          )
          "noPrefix" - checkTree(Glob("**.txt"), expected)
          "prefix" - checkTree(Glob("tree/**.txt"), expected)
        }
      }
    }
  }
}
