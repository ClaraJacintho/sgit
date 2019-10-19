package sgit
import java.text.SimpleDateFormat
import java.util.Calendar
import better.files.Dsl.mkdirs
import better.files.File
import sgit.objects.{Blob, Head, ObjectsHandler, StagingArea}
import scala.annotation.tailrec
import scala.util.matching.Regex

class Sgit(currentDir : File) {
  def help(): Unit = {
    println()
  }

  val gitPath: File = currentDir/".sgit"
  val index: StagingArea = new StagingArea(gitPath/"index")
  val head: Head = new Head(gitPath/"HEAD", gitPath/"refs")
  val objects: ObjectsHandler = new ObjectsHandler(gitPath / "objects")

  /**
   * Creates an sgit repo, with index and head files
   * and objects and refs folders
   */
  def init(): Unit ={
    val files: Array[String] = Array("index", "HEAD")
    if(gitPath.isEmpty) {
      mkdirs(gitPath/ "objects")
      mkdirs(gitPath/ "refs")
    }
    files.map(a=> (gitPath/a).createIfNotExists())
  }

  def isSgitRepo: Boolean =  gitPath.exists

  /**
   * Adds a file to the index and creates a copy on the objects folder
   * @param file - the file that will be added
   */
  def addBlob(file: File): Unit ={
      val sha = file.sha1
      val blob:Blob = new Blob(sha, file)
      objects.addToObjects(file,sha)
      index.addFileToStagingArea(blob)
  }


  /**
   * adds a file, a folder or all files to the staging area
   * @param path - the path of what you want to add, or . for all
   */
  def add(path: String): Unit ={
    if(path == "." || path == "-A" || path == "--all"){
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
        println(s"fatal: pathspec $path did not match any files")
      }
    }
  }

  /**
   * Prints the committed files to the console
   * @param stagedFiles
   * @param branch
   * @param message
   */
  def printCommittedFiles(stagedFiles: Seq[Blob], branch: String, message: String): Unit = {
    Terminal.log("[" + branch + "] "+message)
    stagedFiles.foreach(b => Terminal.log(b.getFileName))
  }

  /**
   * Creates a commit based on the files that are currently staged
   *
   * @param message - the commit message
   */
  def commit(message : String): Unit ={
    val stagedFiles = index.getAllStagedFiles

    File.usingTemporaryFile() {tempFile =>
      tempFile.appendLine(message)
      val now = Calendar.getInstance.getTime
      val date = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z")
      tempFile.appendLine(date.format(now))
      val parent = head.getCurrentCommit
      parent match {
        case Some(s) => tempFile.appendLine(s"parent $s")
        case None => tempFile.appendLine(s"parent")
      }
      stagedFiles.foreach(file => tempFile.appendLine(file.toStringCommit))
      val sha = tempFile.sha1
      objects.addToObjects(tempFile,sha)
      head.addCommitToHead(sha)
      printCommittedFiles(stagedFiles, head.getCurrentBranch, message)
    }
  }

  /***
   * Crates a new branch
   * @param name - the name of the new branch
   * @return True if it was able to create new branch, False if not
   */
  def branch(name:String): Boolean ={
    name match{
      case "-av" =>  listBranches()
                      true
      case _ => head.createBranch(name)
    }

  }

  /***
   * Return files to the state they were in the last commit in the $branch branch
   * @param branch
   * @return True if successful, False if not
   */
  def checkout(branch: String): Boolean ={

    if(!head.checkIfBranchExists(branch)) {
     Terminal.log(s"error: pathspec $branch did not match any file(s) known to sgit")
      false
    } else if(head.getCurrentBranch == branch){
      Terminal.log(s"Already on '$branch'")
      false
    }
    else{
      // If there are uncommitted chages in the staging area that would be overwritten in case of a
      // checkout, we must not checkout!
      val lastCommitOnTargetBranch = head.getLastCommitOnBranch(branch)

      val uncommittedDiff = findOverwrittenFiles(lastCommitOnTargetBranch)

      if(uncommittedDiff.nonEmpty){
        Terminal.log("error: Your local changes to the following files would be overwritten by checkout:")
        uncommittedDiff.foreach(f => Terminal.log(f.toString(), Console.YELLOW))
        Terminal.log("Please commit your changes them before you switch branches. \nAborting ")
        false
      }else {

        val lastCommit = objects.getObject(head.getLastCommitOnBranch(head.getCurrentBranch))
        val committedFilesOnCurrentBranch:Map[String, File] = objects.getCommittedFiles(lastCommit)

        val committedFilesOnTargetBranch = objects.getCommittedFiles(objects.getObject(lastCommitOnTargetBranch))

        // remove files that are committed on current branch but not present in target
        val deleteList = committedFilesOnCurrentBranch.filter(f=> !committedFilesOnTargetBranch.contains(f._1))

        deleteList.foreach(f=>File(f._1).delete())

        // then overwrite/createIfNotExists all committed files
        committedFilesOnTargetBranch.foreach(f =>File(f._1).createIfNotExists().overwrite(f._2.contentAsString))

        // update head to target branch
        head.checkout(branch)

        // clear staging area (?)
        index.clearStaginArea()
        true
      }
    }
  }

  /***
   * Find tracked files that would be overwritten in case fo checkout
   * @param lastCommitOnTargetBranch
   * @return a list (Seq) of files that would be overwritten in case of checkout
   */
  def findOverwrittenFiles(lastCommitOnTargetBranch : String): Seq[File] ={
    val diffTargetBranch = findDiff(lastCommitOnTargetBranch).filter(f=> f._2.nonEmpty).map(_._1)
    val diffCurrBranch = findDiff(head.getLastCommitOnBranch(head.getCurrentBranch)).filter(f=> f._2.nonEmpty).map(_._1)

    // If there is a diff in the current branch => tracked but not committed
    // if the same file has a diff in target branch => would be overwritten and change lost forever!
    diffTargetBranch.filter(f => diffCurrBranch.contains(f))
  }

  /***
   * Compares the current directory with staged files
   */
  def status(): Unit ={
    val stagedBlobs = index.getAllStagedFiles
    val stagedFiles = for{
      file <- stagedBlobs
    } yield file.getFile

    val allFilesInDir = currentDir.listRecursively
                                  .filter(_ != gitPath)
                                  .filter(!_.isChildOf(gitPath))
                                  .filter(!_.isDirectory)
                                  .toList // To make it immutable otherwise the other filters are gonna modify it (even as a val, very weird)

    val untrackedFiles = allFilesInDir.filter(!stagedFiles.contains(_))

    val trackedFilesCurrentVersion = allFilesInDir.filter(stagedFiles.contains(_))
    val trackedFilesCurrentVersionBlob: Map[File, String] = trackedFilesCurrentVersion.map(file => (file, file.sha1)).toMap

    val lastCommit = head.getCurrentCommit match {
      case Some(s) => objects.getObject(s)
      case None => None
    }

    val committedFilesMap:Map[File,String] = objects.getCommittedFilesContent(lastCommit)

    val stagedShas = stagedBlobs.map(_.getSha)

    // STAGED FILE CHANGES VS LAST COMMIT
    val stagedModified = stagedBlobs.filter(b => committedFilesMap.contains(b.getFile))
                                    .filter(b => !committedFilesMap.get(b.getFile).contains(b.getSha))

    val stagedAdd = stagedBlobs.filter(b => !committedFilesMap.contains(b.getFile))

    val stagedDel = committedFilesMap.filter( b=> !stagedFiles.contains(b._1))

    // STAGED FILE CHANGES VS CURRENT DIR

    val currMod = trackedFilesCurrentVersionBlob.filter(b => stagedFiles.contains(b._1))
                                                .filter(b => !stagedShas.contains(b._2))
    val currDel = stagedFiles.filter(f => !trackedFilesCurrentVersion.contains(f))


    println("On branch " + head.getCurrentBranch)
    if(stagedAdd.nonEmpty || stagedDel.nonEmpty || stagedModified.nonEmpty){
      Terminal.log("Changes to be committed:")
      stagedModified.foreach(blob=> Terminal.log(s"     modified: " + currentDir.relativize(blob.getFile), Console.GREEN))
      stagedAdd.foreach(blob=> Terminal.log(s"     added: "+ currentDir.relativize(blob.getFile), Console.GREEN))
      stagedDel.foreach(blob=> Terminal.log(s"     deleted: "+ currentDir.relativize(blob._1), Console.GREEN))
    }

    if(currDel.nonEmpty || currMod.nonEmpty){
      println(Console.RESET +"Changes not staged for commit:")
      println("""   (use "sgit add <file>..." to update what will be committed)""")
      currMod.foreach(blob=> println(Console.RED + s"     modified: " + currentDir.relativize(blob._1)))
      currDel.foreach(blob=> println(Console.RED + s"     deleted: "+ currentDir.relativize(blob)))
    }
    if(untrackedFiles.nonEmpty){
      println(Console.RESET + """Untracked files:
                |  (use "sgit add <file>..." to include in what will be committed)""".stripMargin)
      untrackedFiles.foreach(file=> println(Console.RED + s"      "+ currentDir.relativize(file)))
    }
  }

  /***
   * Show history of commits on current branch
   */
  def log(): Unit ={
    val commit = head.getCurrentCommit match {
      case Some(s) => s
      case None => Terminal.log("fatal: your current branch does not have any commits yet ")
                   return
    }
    printCommit(commit)
    logRec(objects.getParent(commit))
  }

  /***
   * Goes through past commits and prints them
   * @param sha
   */
  @tailrec
  final def logRec(sha:String): Unit ={
    if(sha.isEmpty) return
    else printCommit(sha)
    val parent = objects.getParent(sha)
    logRec(parent)
  }

  /***
   * Prints commits to terminal
   * @param sha
   */
  def printCommit(sha : String): Unit ={
    val commit = objects.getObject(sha)
    val date = objects.getCommitDate(commit)
    val message = objects.getCommitMessage(commit)

    Terminal.log(s"commit $sha", Console.YELLOW)
    Terminal.log(s"Date $date")
    Terminal.log("")
    Terminal.log(s"    $message")
  }

  /***
   * Finds diff bewteen staging area and other objects (according to arg) and prints it
   *
   * @param arg
   *      if empty, does staging area vs last commit
   *      if file, compares staged file with version on current dir
   *      if commit (sha), compares staging area with that commit
   */
  def diff(arg :String): Unit ={
    val diff = findDiff(arg)
    diff.foreach(d => printDiff(d._1, d._2))
  }

  /***
   * Finds diffences in the content of staged files and other the current dir/last commit/a particular commit
   *
   * @param arg
   *      if empty, does staging area vs last commit
   *      if file, compares staged file with version on current dir
   *      if commit (sha), compares staging area with that commit
   * @return e list (seq) of files with diffs
   */
  def findDiff(arg :String): Seq[(File, Seq[(String, Int, String)])] = {
    if (arg.nonEmpty && objects.getObject(arg).nonEmpty){
      for {
        f <- findFilesCommit(arg)
      } yield (f._2, diffFiles(f._1, f._2))

    } else {
      for {
        f <- findFilesInCurrentDir(arg)
      } yield (f._2, diffFiles(f._1, f._2))
    }
  }

  /***
   * Find files from a commit to compare with the current staging area
   * @param commit
   * @return pairs of files that exist in both the commit and staging area
   */
  def findFilesCommit(commit : String): Seq[(File, File)] = {
    val committedFiles = objects.getCommittedFiles(objects.getObject(commit))
    val stagedFiles = index.getAllStagedFiles
    stagedFiles.filter(f=>committedFiles.contains(f.getFileName))
      .map(f=>
        (committedFiles(f.getFileName), f.getFile)
      )
  }

  /***
   * Finds files from the current dir to compare with the staging area
   * @param arg - either a file or nothing, for all files in the cwd
   * @return  pairs of files that exist in both
   */
  def findFilesInCurrentDir(arg : String): Seq[(File, File)] ={
    if(arg.isEmpty){
       """(.*) \d (.*)""".r
        .findAllIn(index.indexAsString)
        .matchData
        .map(m => (objects.getObject(m.group(1)).getOrElse(File("")), File(m.group(2)) )).toSeq

    } else  if ((currentDir/arg).isRegularFile) {

     ("""(.*) \d (""" + Regex.quote((currentDir/arg).toString()) +""")""").r
       .findAllIn(index.indexAsString)
       .matchData
       .map(m => (objects.getObject(m.group(1)).getOrElse(File("")), File(m.group(2)) )).toSeq

    } else {
      Terminal.log(s"fatal: ambiguous argument $arg: unknown revision or path not in the working tree.")
      Seq()
    }

  }

  /***
   * Given two versions of a file, finds the differences in them
   * @param a version 1
   * @param b version 2
   * @return a sorted array of lines with an index, a + or - and the line
   */
  def diffFiles(a:File, b:File): Seq[(String, Int, String)] ={
    val linesA = if(a.exists) a.lines.zipWithIndex.toSeq else Seq()
    val linesB = if(b.exists) b.lines.zipWithIndex.toSeq else Seq()
    val min = (linesA diff linesB).map(e => ("-", e._2, e._1))
    val add = (linesB diff linesA).map(e => ("+", e._2, e._1))
    val arr = min.concat(add).sortBy(_._2)
    arr
  }

  /***
   * Prints the diffs
   * @param file - the file for which the diff was found
   * @param diff - the diffs
   */
  def printDiff(file: File, diff: Seq[(String, Int, String)]): Unit ={
    println(file)
    diff.foreach(d =>
        if(d._1 == "+"){
         Terminal.log(d._1 + " " + d._2 + " " + d._3, Console.GREEN)
        }else{
          Terminal.log(d._1 + " " + d._2 + " " + d._3, Console.RED)
        }
    )
  }

  /**
   * Lists existing branches, latest commit on each and its message
   */
    def listBranches() = {
      val branches = head.listBranches()
                         .map(f =>
                                (f, objects.getObject(f.contentAsString))
                          )

      branches.foreach(b =>
                  Terminal.log(b._1.toString() + " " + b._1.contentAsString + " " + objects.getCommitMessage(b._2)))
    }
  /***
   * Created with the blood, sweat and tears of Clara Jacintho :)
   */
  def credits = {
   Terminal.log(File("ascii.txt").contentAsString, Console.BLUE)
  }



}
