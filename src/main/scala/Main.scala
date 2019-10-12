import better.files._
import better.files.Dsl._
import java.nio.file.{Files => JFile}
import sgit.Sgit
object Main {

  def main(args: Array[String]): Unit = {
    val git = new Sgit(cwd/"TestDir")
    println("Existence is pain")
    git.init()
    git.add("foo.txt")
    git.status()
    //git.add("lapin/hello/test.txt")
    git.commit("commit on branch foo")
    //git.branch("bar")

    /*
    git.add("foo.txt")
    git.add("lapin")
    */
  }
}


