import better.files._
import better.files.Dsl._

object SGIT {
  def main(args: Array[String]): Unit = {
    println("Existence is pain")
    init()
  }

  def init(): Unit ={
    val gitPath = cwd/".sgit"
    val files = Array("index", "HEAD")
    if(gitPath.isEmpty)
      mkdirs(gitPath/"objects")
      files.map(a=> (gitPath/a).createIfNotExists())
  }
}
