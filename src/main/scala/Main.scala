import better.files._
import better.files.Dsl._
import java.nio.file.{Files => JFile}
import sgit.Sgit
object Main {

  def main(args: Array[String]): Unit = {
    val git = new Sgit(cwd)
    println("Existence is pain")

    if(args.length == 0){
      git.help()
    } else if(args(0) == "init"){
      git.init()
    } else {
      if(!git.isSgitRepo){ //make a isSgitRepo func
        println("fatal: not a sgit repository")
      } else {
        args(0) match {
          case "add" => git.add(args(1))
          case "commit" => git.commit(args(1))
          case "branch" => git.branch(args(1))
          case "log" => git.log()
          case "status" => git.status()
          case "diff" => if(args.length > 1) git.diff(args(1)) else git.diff("")
          case "checkout" => git.checkout(args(1))
          case "help" => git.help()
          case other => println(s"sgit: $other is not a sgit command. see sgit help")
        }
      }
    }

  }
}


