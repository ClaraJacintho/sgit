package sgit.objects

import better.files.Dsl.mkdir
import better.files.File

class Objs(path: File) {
  /**
   * Adds a file to the objects folder
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

  def getObject(sha: String): Option[File] ={
    val folder = path/ sha.substring(0, 2)
    val fileName = sha.substring(2)
    if ((folder / fileName).isRegularFile) {
      Some(folder / fileName)
    }
    else
      None
  }

  def getParent(sha:String): String ={
    val parentRegex = """parent (.*)""".r
    val commit : String= getObject(sha) match {
      case Some(s) => s.contentAsString
      case None => ""
    }
     val parent = for (m <- parentRegex.findFirstMatchIn(commit)) yield m.group(1)
     parent.getOrElse("")
  }

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

}
