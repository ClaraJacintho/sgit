import better.files._
import better.files.Dsl._
import java.nio.file.{Files => JFile}
object SGIT {
  def main(args: Array[String]): Unit = {
    println("Existence is pain")
    init()
    add("foo.txt")
  }

  def init(): Unit ={
    val gitPath = cwd/".sgit"
    val files = Array("index", "HEAD")
    if(gitPath.isEmpty)
      mkdirs(gitPath/"objects")
      files.map(a=> (gitPath/a).createIfNotExists())
  }

  def addBlob(file: File): Unit ={
    //TODO: Check in index if this file has already been added
    val gitPath = cwd/".sgit"
    val sha = file.sha1
    val folder = gitPath/"objects"/sha.substring(0,2)
    val fileName = sha.substring(2)
    if(!folder.isDirectory)
      mkdir(folder)
    file.copyToDirectory(folder).renameTo(fileName)
    val index = gitPath/"index"
    index.append(sha+" "+ (file.toString().toSeq.diff(cwd.toString())))
  }
  def add(path: String): Unit ={
    if(path == "."){
      //TODO: Add all file changes (including removes etc)
    }
    else{
      val file = cwd/path
      if (file.isRegularFile){
        println("File")
        addBlob(file)
      }else if(file.isDirectory){
        println("Folder")
      } else{
        //Throw????
        println(s"fatal: pathspec $path did not match any files")
      }
    }
  }
}
