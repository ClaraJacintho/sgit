package sgit.objects

import better.files.File

class Blob(sha : String, file: File, modifier: Int = 0){
  def toStringIndex: String = {
    //var name = currentDir.relativize(file).toString
    s"$sha $modifier $file" // maybe not string => write bytes
  }
  def toStringCommit: String ={
    s"$sha $file"
  }

  def getFileName: String = {
    this.file.toString()
  }

  def getFile: File = {
    this.file
  }

  def getSha: String = this.sha

  override def toString: String = s"$sha $file"
}
class Tree(sha : String = "", file: File = File("")){
  def toStringTree = s"tree $sha $file"
}
/*
class Commit(tree: Tree, message: String){
  def toStringCommit: String = {
    tree.toStringTree + "\n" + message
  }
}
*/

class Commit(file: File){
  def getCommitedFiles: Seq[String] ={
     file.lines.toSeq.tail
  }
}
