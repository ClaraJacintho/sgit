package sgit.objects

import better.files.Dsl.mkdir
import better.files.File

class ObjectsHandler(path: File) {
  /**
   * Adds a blob or commit to the objects folder
   * @param file - the file to be added (file, tree, commit)
   * @param sha - the sha1 string
   */
  def addToObjects(file: File, sha: String): Unit ={
    val folder = path/ sha.substring(0, 2)
    val fileName = sha.substring(2)
    if (!(folder / fileName).isRegularFile) {
      if (!folder.isDirectory)
        mkdir(folder)
      file.copyToDirectory(folder).renameTo(fileName) //zip?
    }
  }

  /***
   * given a sha, returns the file if it exits on the objects folder
   * @param sha - the commit
   * @return a file with the respective sha if it exists, None if not
   */
  def getObject(sha: String): Option[File] ={
    val folder = path/ sha.substring(0, 2)
    val fileName = sha.substring(2)
    if ((folder / fileName).isRegularFile) {
      Some(folder / fileName)
    }
    else
      None
  }

  /**
   * Given a commit (as sha), returns a parent if it has it
   * @param sha - the commit
   * @return the sha of the parent if it exists
   */
  def getParent(sha:String): String ={
    val parentRegex = """parent (.*)""".r
    val commit : String= getObject(sha) match {
      case Some(s) => s.contentAsString
      case None => ""
    }
     val parent = for (m <- parentRegex.findFirstMatchIn(commit)) yield m.group(1)
     parent.getOrElse("")
  }

  /**
   * gets the files of a given commit
   * @param commit
   * @return a map where the file name is the key, the val is the file
   */
  def getCommittedFiles(commit : Option[File]) : Map[String, File] = {
    val committedBlobs : Seq[String] = commit match {
        case Some(f) =>  f.lines.toSeq.drop(3)

        case None => Seq()
      }

    val fileNameRegex = """(.*) (.*)""".r
    committedBlobs.map(
      entry =>
        fileNameRegex.findAllIn(entry).matchData.map(m=> m.group(2) -> getObject(m.group(1)) )
                                                .filter(x => x._2.nonEmpty)
                                                .map(f => f._1 -> f._2.get)
                                                .toSeq.head
    ).toMap
  }

  /**
   * gets the content of each file for a commit
   * @param commit
   * @return a map where the file name is the key, the val is the content
   */
  def getCommittedFilesContent(commit : Option[File]) : Map[File,String] = {
    val committedBlobs : Seq[String] = commit match {
      case Some(commit) => commit.lines.toSeq.drop(3)
      case None => Seq()
    }

    val fileNameRegex = """(.*) (.*)""".r

    committedBlobs.map(
      entry =>
        fileNameRegex.findAllIn(entry).matchData.map(m=> File(m.group(2)) -> m.group(1)).toSeq.head
    ).toMap
  }

  def getCommitDate(commit: Option[File]) : String = {
    commit match {
      case Some(s) => s.lines.toSeq(1)
      case None => ""// nothing
    }
  }

  def getCommitMessage(commit: Option[File]) : String = {
    commit match {
      case Some(s) => s.lines.toSeq(0)
      case None => ""// nothing
    }
  }
}
