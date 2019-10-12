package sgit.objects

import better.files.File

class Head(head: File, refs: File) {
  val refHeads: File = refs/"heads"
  val refTags: File = refs/"tags"

  def addCommitToHead(sha: String): Unit ={
    val branch = getCurrentBranch match {
      case Some(s) => s
      case None => "master"
    }
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

  def getCurrentBranch:Option[String]={
    if(!head.isEmpty)
      Some(head.lines.toSeq(0))
    else
      None
  }

  def createBranch(name: String): Unit ={
    refHeads.createDirectoryIfNotExists()
    if((refHeads/name).exists){
      print(s"fatal: A branch named $name already exists.")
    }else{
      val line = getCurrentCommit match{
        case Some(s) => (refHeads/name).createFile().appendLine(s)
                        head.clear().appendLine(name).appendLine(s)
        case None => head.appendLine(name)
      }

    }
  }

}
