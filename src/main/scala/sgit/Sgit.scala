package sgit

import better.files.Dsl.{cwd, mkdir, mkdirs}
import better.files.File
import sgit.objects
import sgit.objects.Blob

import scala.util.matching.Regex

class Sgit(currentDir : File) {
  val gitPath: File = currentDir/".sgit"
  val index: File =  gitPath/"index"
  val head: File = gitPath/"HEAD"


  def init(): Unit ={
    val files = Array("index", "HEAD")
    if(gitPath.isEmpty)
      mkdirs(gitPath/ "objects")
    files.map(a=> (gitPath/a).createIfNotExists())
  }

  def addBlob(file: File): Unit ={
      val sha = file.sha1
      val folder = gitPath / "objects" / sha.substring(0, 2)
      //TODO: add blob + file size (header" to blob content before sha ???? no tho
      val fileName = sha.substring(2)
      if (!(folder / fileName).isRegularFile) {
        if (!folder.isDirectory)
          mkdir(folder)
        file.copyToDirectory(folder).renameTo(fileName) //zip?
      }
      // TODO: Optimize name finding?
      val ogFileName = currentDir.relativize(file).toString //This is the most beautiful thing i've ever seen
      addFileToIndex(ogFileName, sha)
  }


  //TODO: Não funciona se tu tenta adicionar de um sub dir!
  // Digamos q o o root seja lapin e tenha um subdir toto
  // e tu tenta add um file de toto
  // O problema é q pega currentDir errado => solução? olhar pra
  // dir acima até achar?
  def addFileToIndex(name: String, sha: String): Unit ={
    val fileNameRegex = ("""(.* [0-3] """+ Regex.quote(name) + """)""" ).r
    val indexEntry : String = sha + " 0 " + name

    if(fileNameRegex.findFirstIn(index.contentAsString).nonEmpty) {
      index.
        overwrite( // is this functional????
          fileNameRegex.replaceAllIn(index.contentAsString, Regex quoteReplacement indexEntry)
        )
    } else {
      index.appendLine(indexEntry)
    }
  }

  def add(path: String): Unit ={
    if(path == "." || path == "-A" || path == "--all"){
      // Wasteful! Stupid!
      // TODO: Refactor
      index.clear()
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
      val fileNameRegex = """(.*) \d (.*)""".r
      val stagedFiles: Seq[(String, String)] = fileNameRegex.findAllIn(index.contentAsString)
                                      .matchData
                                      .map(m => (m.group(1), m.group(2))).toSeq

      var commitTree : Seq[String] = Seq()
      stagedFiles.foreach(
      obj =>
        if((currentDir/obj._2).parent == currentDir){ // if the file is not in the folder
          commitTree = commitTree:+ new Blob(obj._1, obj._2).toStringCommit
        } else{
          println( (currentDir/obj._2).parent )
        }
      )
     commitTree.foreach(println)
    /*
      File.usingTemporaryFile() {tempFile =>
        //do something
      }
     */
  }

  def createTree(path: File): Unit ={

  }

}
