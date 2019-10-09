import better.files._
import better.files.Dsl._
import java.nio.file.{Files => JFile}
import sgit.Sgit
object Main {

  def main(args: Array[String]): Unit = {
    val git = new Sgit(cwd/"TestDir")
    println("Existence is pain")
    git.init()
    git.add(".")
    //git.add("lapin/hello/test.txt")
    git.commit("foo")
    /*
    git.add("foo.txt")
    git.add("lapin")
    */
  }
}


