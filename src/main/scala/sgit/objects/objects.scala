package sgit.objects

import better.files.File

class Blob(sha : String, file: File, modifier: Int = 0){
  def toStringIndex: String = {
    //var name = currentDir.relativize(file).toString
    s"$sha $modifier $file" // maybe not string => write bytes
  }
  def toStringCommit: String ={
    s"blob $sha $file"
  }

  def getFileName: String = {
    this.file.toString()
  }

  def getFile: File = {
    this.file
  }
}
class Tree{

}

class commit {

}

class Folder(path: File){

}
