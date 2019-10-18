package sgit.objects

import java.util.Calendar
import java.text.SimpleDateFormat
import better.files.File

class Head(head: File, refs: File) {
  val refHeads: File = refs/"heads"
  val refTags: File = refs/"tags"

  def addCommitToHead(sha: String): Unit ={
    val branch = getCurrentBranch
    val parent = getCurrentCommit
    refHeads.createDirectoryIfNotExists()
    (refHeads/branch).createIfNotExists().overwrite(sha)
    head.clear()
    head.appendLine(s"$branch").appendLine(sha)
  }

  def getCurrentCommit: Option[String] ={
    if(!head.isEmpty) {
      Some(head.lines.toSeq(1))
    }
    else
      None
  }

  def getCurrentBranch:String={
    if(!head.isEmpty)
      head.lines.toSeq(0)
    else
      "master"
  }

  def getParent(sha:String): Option[String] ={
    if(!head.isEmpty && head.lines.toSeq.length > 3){
      Some(head.lines.toSeq(3))
    } else
      None
  }

  def createBranch(name: String): Boolean ={
    refHeads.createDirectoryIfNotExists()
    if((refHeads/name).exists){
      print(s"fatal: A branch named $name already exists.")
      false
    }else{
      val line = getCurrentCommit match{
        case Some(s) => (refHeads/name).createFile().appendLine(s)
                        head.clear().appendLine(name).appendLine(s)
        case None => head.appendLine(name)
      }
      true
    }

  }

  def checkIfBranchExists(branch: String): Boolean ={
    if((refHeads/branch).exists)
      true
    else
      false
  }

  def getLastCommitOnBranch(branch:String) : String = {
    (refHeads/branch).contentAsString
  }

  def getCommittedFilesCurrentVersion(commit : Option[File]) : Map[File,String] = {
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

  def checkout(branch : String): Unit ={
      head.clear()
        .appendLine(branch)
        .appendLine(getLastCommitOnBranch(branch))
  }


}
