import better.files._
import better.files.Dsl._
import java.nio.file.{Files => JFile}
import sgit.Sgit
object Main {

  def main(args: Array[String]): Unit = {
    val git = new Sgit(cwd/"TestDir")
    println("Existence is pain")
    git.init()
    git.add("lapin/tata.txt")
    git.commit("first")
    git.add("foo.txt")
    git.commit("second")
    git.add("lapin")
    git.commit("third")
    git.log()


  }
}


