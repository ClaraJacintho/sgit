package sgit

import better.files.Dsl.{cwd, mkdir, mkdirs}
import better.files.File
import sgit.objects
import sgit.objects.{Blob, StagingArea}

import scala.util.matching.Regex

class Sgit(currentDir : File) {
  val gitPath: File = currentDir/".sgit"
  val index: StagingArea = new StagingArea(gitPath/"index")
  val head: File = gitPath/"HEAD"


  def init(): Unit ={
    val files = Array("index", "HEAD")
    if(gitPath.isEmpty)
      mkdirs(gitPath/ "objects")
    files.map(a=> (gitPath/a).createIfNotExists())
  }

  def addBlob(file: File): Unit ={
      val sha = file.sha1
      val blob:Blob = new Blob(sha, file)
      val folder = gitPath / "objects" / sha.substring(0, 2)
      //TODO: add blob + file size (header" to blob content before sha ???? no tho
      val fileName = sha.substring(2)
      if (!(folder / fileName).isRegularFile) {
        if (!folder.isDirectory)
          mkdir(folder)
        file.copyToDirectory(folder).renameTo(fileName) //zip?
      }

      index.addFileToStagingArea(blob)
  }


  def add(path: String): Unit ={
    if(path == "." || path == "-A" || path == "--all"){
      // Wasteful! Stupid!
      // TODO: Refactor
      index.clearStaginArea()
      currentDir
        .listRecursively.toSeq
        .filter(!_.isDirectory)
        .filter(!_.isChildOf(gitPath))
        .foreach(addBlob)
    }
    else{
      val file = currentDir/path
      if (file.isRegularFile){
        addBlob(file)
      }else if(file.isDirectory){
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
      // Regex to get all filenames
      // uuh parse to get all directories => list comp thing
      // create trees to all dirs
      // create tree for commit (dir trees + files on root)
      // create commit, if head not empty put parent in second line
      // add msg
      // diff?
      val stagedFiles = index.getAllStagedFiles()

      //THIS LINE TOOK ME 2 HOURS
      val stagedFolders = stagedFiles.map(blob => currentDir.relativize(blob.getFile()).getParent).filter(_ != null).distinct
      println(stagedFolders)





    /*
      var commitTree : Seq[String] = Seq()
      stagedFiles.foreach(
      obj =>
        if((currentDir/obj._2).parent == currentDir){ // if the file is not in the root folder
          commitTree = commitTree:+ new Blob(obj._1, obj._2).toStringCommit
        } else{
          //println( File(obj._2).parent )
        }
      )
      stagedFiles.groupBy(obj => File(obj._2).parent).foreach(println)
      println((currentDir/"lapin/hello").isSiblingOf((currentDir/"lapin/tata.txt")))
     //commitTree.foreach(println)

      File.usingTemporaryFile() {tempFile =>
        //do something
      }
     */
  }



  def createTree(folder: File): String ={
      val obj = folder.listRecursively
        .map(file =>
            if(file.isDirectory){
              createTree(file)
            }else{
              //new Blob()
            }
        )
    "fuck"
  }

}
