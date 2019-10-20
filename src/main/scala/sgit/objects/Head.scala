package sgit.objects

import java.util.Calendar
import java.text.SimpleDateFormat

import better.files.File
import sgit.Terminal

class Head(head: File, refs: File) {

  val refHeads: File = refs/"heads"
  val refTags: File = refs/"tags"

  /***
   * Adds a commit to the HEAD file, creats the refs dir if it doesnt exist
   * @param sha - the object that contais the commit
   */
  def addCommitToHead(sha: String): Unit ={
    val branch = getCurrentBranch
    val parent = getCurrentCommit
    refHeads.createDirectoryIfNotExists()
    (refHeads/branch).createIfNotExists().overwrite(sha)
    head.clear() // Functional?
    head.appendLine(s"$branch").appendLine(sha)
  }

  /***
   * Gets the latest commit (sha) on the current branch
   * @return commit's sha
   */
  def getCurrentCommit: Option[String] ={
    if(!head.isEmpty) {
      Some(head.lines.toSeq(1))
    }
    else
      None
  }

  /***
   * Gets the current branch, if it exists, defaults to master otherwise
   * @return branch name
   */
  def getCurrentBranch:String={
    if(!head.isEmpty)
      head.lines.toSeq(0)
    else
      "master"
  }


  /***
   * gets the parent commit of a commit
   * @param sha - the commit
   * @return the parent commit's sha
   */
  def getParent(sha:String): Option[String] ={
    if(!head.isEmpty && head.lines.toSeq.length > 3){
      Some(head.lines.toSeq(3))
    } else
      None
  }

  /***
   * creates a new branch if it doesnt exit
   * @param name - the name of the new branch
   * @return true if the branch was created, false otherwise
   */
  def createBranch(name: String): Boolean ={
    refHeads.createDirectoryIfNotExists()
    if((refHeads/name).exists){
      Terminal.log(s"fatal: A branch named $name already exists.")
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

  /***
   *  check if a branch exists
   * @param branch - the branch that will be checked
   * @return true if it exist, false if not
   */
  def checkIfBranchExists(branch: String): Boolean ={
    if((refHeads/branch).exists)
      true
    else
      false
  }

  /***
   * get the last commit on a given branch
   * @param branch - target branch
   * @return the sha of the last commit on the branch
   */
  def getLastCommitOnBranch(branch:String) : String = {
    (refHeads/branch).contentAsString
  }

  /***
   * updates head after a successful checkout
   * @param branch - target branch
   */
  def checkout(branch : String): Unit ={
      head.clear()
        .appendLine(branch)
        .appendLine(getLastCommitOnBranch(branch))
  }

  def listBranches(): Seq[File] = {
    // list ref/heads
    refHeads.list.toSeq.concat(refTags.list.toSeq)
  }

  /***
   * creates a new branch if it doesnt exit
   * @param name - the name of the new branch
   * @return true if the branch was created, false otherwise
   */
  def createTag(name: String): Boolean ={
    refTags.createDirectoryIfNotExists()
    if((refTags/name).exists){
      Terminal.log(s"fatal: A tag named $name already exists.")
      false
    }else{
      getCurrentCommit match{
        case Some(s) => (refTags/name).createFile().append(s)
        case None => false
      }
      true
    }
  }

}
