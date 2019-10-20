import better.files._
import better.files.Dsl._
import java.nio.file.{Files => JFile}

import sgit.{Sgit, Terminal}
object Main {

  def main(args: Array[String]): Unit = {
    val git = new Sgit(cwd)
    if(args.length == 0){
      git.help()
    } else if(args(0) == "init"){
      git.init()
    } else {
      if(!git.isSgitRepo){ //make a isSgitRepo func
        println("fatal: not a sgit repository", Console.RED)
      } else {
        args(0) match {
          case "add" => if(args.length > 1) git.add(args(1)) else git.add(".")
          case "commit" =>if(args.length > 1)  git.commit(args(1)) else Terminal.log("error: commit requires a message")
          case "branch" => if(args.length > 1) git.branch(args(1)) else Terminal.log("error: branch requires a branch name")
          case "tag" => if(args.length > 1) git.tag(args(1)) else Terminal.log("error: tag requires a branch name")
          case "log" => git.log()
          case "status" => git.status()
          case "diff" => if(args.length > 1) git.diff(args(1)) else git.diff("")
          case "checkout" => if(args.length > 1)  git.checkout(args(1)) else Terminal.log("error: checkout requires a branch name")
          case "help" => git.help()
          case "credits" => git.credits()
          case other => Terminal.log(s"sgit: $other is not a sgit command. see sgit help")
        }
      }
    }

  }
}


