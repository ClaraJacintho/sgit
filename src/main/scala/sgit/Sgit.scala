package sgit

import better.files.Dsl.{cwd, mkdir, mkdirs}
import better.files.File

import scala.util.matching.Regex

class Sgit(currendDir : File) {
  val gitPath: File = currendDir/".sgit"
  val index: File =  gitPath/"index"
  val head: File = gitPath/"HEAD"


  def init(): Unit ={
    val files = Array("index", "HEAD")
    if(gitPath.isEmpty)
      mkdirs(gitPath/"objects")
    files.map(a=> (gitPath/a).createIfNotExists())
  }

  def addBlob(file: File): Unit ={
    //TODO: Check in index if this file has already been added
      val sha = file.sha1
      val folder = gitPath / "objects" / sha.substring(0, 2)
      //TODO: add blob + file size (header" to blob content before sha
      val fileName = sha.substring(2)
      if (!(folder / fileName).isRegularFile) {
        if (!folder.isDirectory)
          mkdir(folder)
        file.copyToDirectory(folder).renameTo(fileName) //zip?
      }
      // TODO: Optimize name finding?
      val ogFileName = file.toString().toSeq.diff(currendDir.toString()).toString() //This is the ugliest thing i've ever seen
      addFileToIndex(ogFileName, sha)
  }

  def addFileToIndex(name: String, sha: String): Unit ={
    val fileNameRegex = ("""(.* [0-3] """+ Regex.quote(name) + """)""" ).r
    val indexEntry : String = sha + " 0 " + name + "\n"

    if(fileNameRegex.findFirstIn(index.contentAsString).nonEmpty) {
      index.
        overwrite( // is this functional????
          fileNameRegex.replaceAllIn(index.contentAsString, Regex quoteReplacement indexEntry)
        )
    } else {
      index.append(indexEntry)
    }
  }

  def add(path: String): Unit ={
    if(path == "." || path == "-A" || path == "--all"){
      // Wasteful! Stupid!
      // TODO: Refactor
      index.clear()
      currendDir
        .listRecursively.toSeq
        .filter(!_.isDirectory)
        .filter(!_.isChildOf(gitPath))
        .foreach(addBlob)
    }
    else{
      val file = cwd/path
      if (file.isRegularFile){
        addBlob(file)
      }else if(file.isDirectory){
        //TODO: find out how to create trees for each folder when committing
        file.listRecursively.toSeq
          .filter(!_.isDirectory)
          .foreach(addBlob)
      } else{
        //Throw????
        println(s"fatal: pathspec $path did not match any files")
      }
    }
  }

  def commit(message : String): Unit ={

  }
}
