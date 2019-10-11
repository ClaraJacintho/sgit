package sgit.objects

import better.files.File

class Head(head: File, refs: File) {
  def addCommitToHead(sha: String): Unit ={
    if(head.isEmpty){
      //master
      (refs/"master").createFile().appendLine(sha)
      head.appendLine("branch: master")
      head.appendLine(sha)
    }else{
      val branchRegex = "branch: (.*)".r
      val branch = branchRegex.findFirstIn(head.contentAsString).get
      (refs/branch).overwrite(sha)
      head.overwrite(s"branch: $branch")
      head.appendLine(sha)
    }
  }

}
