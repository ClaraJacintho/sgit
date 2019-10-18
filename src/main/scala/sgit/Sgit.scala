package sgit

import java.text.SimpleDateFormat
import java.util.{Calendar, Objects}

import better.files.Dsl.mkdirs
import better.files.File
import sgit.objects.{Blob, Commit, Head, Objs, StagingArea, Tree}
import scala.io.StdIn.readLine
import scala.annotation.tailrec
import scala.util.matching.Regex

class Sgit(currentDir : File) {
  def help(): Unit = {
    println()
  }

  val gitPath: File = currentDir/".sgit"
  val index: StagingArea = new StagingArea(gitPath/"index")
  val head: Head = new Head(gitPath/"HEAD", gitPath/"refs")
  val objects: Objs = new Objs(gitPath / "objects")

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
   * Creates a commit based on the files that are currently staged
   * @param message - the commit message
   */
  def commit(message : String): Unit ={
    val stagedFiles = index.getAllStagedFiles
      /*
      This part is for working with trees, but i decided it's too complex and adds literally nothing but more work
      But it was so complex to do I think it deserves to stay in the code until the very end

      //THIS LINE TOOK ME 2 HOURS
      val stagedFolders = stagedFiles.map(blob => blob.getFile.parent).filter(b=> b != currentDir).distinct

      val tree = createTree(currentDir, stagedFolders, stagedFiles.filter(blob => blob.getFile.isChildOf(currentDir)))

      File.usingTemporaryFile() {tempFile =>
        val commit = new Commit(tree,message)
        tempFile.appendLine(commit.toStringCommit)
        val sha = tempFile.sha1
        addToObjects(tempFile,sha)
        head.addCommitToHead(sha)
      }
      */

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
    }
  }

  /**
   * Creates a tree from a folder (including all its subdirectories)
   * and adds the tree to the objects folder
   * @param folder - the folder you want to make into a tree
   * @param stagedFolders - all folders currently staged
   * @param stagedFiles - all files currently staged
   * @return the Tree object with the reference to the folder's tree
   */
  def createTree(folder: File, stagedFolders: Seq[File], stagedFiles: Seq[Blob]): Tree ={
    var resultingTree : Tree = new Tree()
    File.usingTemporaryFile() {tempFile =>
      val allFoldersInDir = folder.list.toSeq.filter(_.isDirectory).filter(_ != gitPath)
      stagedFiles.filter(blob=> blob.getFile.parent == folder).foreach(blob=>
        tempFile.appendLine(blob.toStringCommit)
      )

      val trees: Seq[Tree] = for{
        dir <- allFoldersInDir
      } yield createTree(dir, stagedFolders, stagedFiles.filter(blob =>blob.getFile.isChildOf(dir)))

      trees.distinct.foreach(t => tempFile.appendLine(t.toStringTree))
      val sha = tempFile.sha1
      resultingTree = new Tree(sha, folder)
      objects.addToObjects(tempFile,sha)
    }
    resultingTree
  }

  def branch(name:String): Boolean ={
    head.createBranch(name)
  }

  def checkout(branch: String): Unit ={
    // see if branch exists
    // get last commit on target branch
    // copy files back (overwrite current versions if necessary (how?)
    //  delete everything (check if diff exists? if yes dont checkout)
    if(!head.checkIfBranchExists(branch)) {
      println(s"error: pathspec $branch did not match any file(s) known to sgit")
    } else if(head.getCurrentBranch == branch){
      println(s"Already on '$branch'")
    }
    else{
      val lastCommit = objects.getObject(head.getLastCommitOnBranch(head.getCurrentBranch))
      val committedFilesOnCurrentBranch:Map[String, File] = objects.getCommittedFiles(lastCommit)
      // diff between current staging area and target branch last commit, if diff dont change
      val lastCommitOnTargetBranch = head.getLastCommitOnBranch(branch)
      val diffTargetBranch = findDiff(lastCommitOnTargetBranch).filter(f=> f._2.nonEmpty).map(_._1)
      val diffCurrBranch = findDiff("").filter(f=> f._2.nonEmpty).map(_._1)

      val uncommittedDiff = diffTargetBranch.filter(f => diffCurrBranch.contains(f))
      if(uncommittedDiff.nonEmpty){
        println("error: Your local changes to the following files would be overwritten by checkout:")
        diffTargetBranch.foreach(println)
        println("Please commit your changes or stash them before you switch branches. \nAborting ")
      }else {
        // remove files that are committed on current branch but not present in target branch
        val committedFilesOnTargetBranch = objects.getCommittedFiles(objects.getObject(lastCommitOnTargetBranch))
        val deleteList = committedFilesOnCurrentBranch.filter(f=> !committedFilesOnTargetBranch.contains(f._1))

        deleteList.foreach(f=>File(f._1).delete())

        // then overwrite/createIfNotExists all committed files
        committedFilesOnTargetBranch.foreach(println)
        committedFilesOnTargetBranch.foreach(f =>File(f._1).createIfNotExists().overwrite(f._2.contentAsString))

        head.checkout(branch)
        index.clearStaginArea()
      }
    }
  }

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

    val committedFilesMap:Map[File,String] = head.getCommittedFilesCurrentVersion(lastCommit)

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
      println("Changes to be committed:")
      stagedModified.foreach(blob=> println(Console.GREEN + s"     modified: " + currentDir.relativize(blob.getFile)))
      stagedAdd.foreach(blob=> println(Console.GREEN +s"     added: "+ currentDir.relativize(blob.getFile)))
      stagedDel.foreach(blob=> println(Console.GREEN +s"     deleted: "+ currentDir.relativize(blob._1)))
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

  def log(): Unit ={
    val commit = head.getCurrentCommit match {
      case Some(s) => s
      case None => println("fatal: your current branch does not have any commits yet ")
                   return
    }
    printCommit(commit)
    logRec(objects.getParent(commit))
  }

  @tailrec
  final def logRec(sha:String): Unit ={
    if(sha.isEmpty) return
    else printCommit(sha)
    val parent = objects.getParent(sha)
    logRec(parent)
  }

  def printCommit(sha : String): Unit ={
    val commit = objects.getObject(sha)
    val date = commit match {
      case Some(s) => s.lines.toSeq(1)
      case None => ""// nothing
    }
    val message = commit match {
      case Some(s) => s.lines.toSeq(0)
      case None => ""// nothing
    }

    println(Console.YELLOW + s"commit $sha")
    println(Console.RESET + s"Date $date")
    println()
    println(s"    $message")
  }

  def diff(arg :String): Unit ={
    val diff = findDiff(arg)
    diff.foreach(d => printDiff(d._1, d._2))
  }
  def findDiff(arg :String): Seq[(File, Seq[(String, Int, String)])] = {
    var filesToCompare : Seq[(File, File)] = Seq()
    var fileNameRegex = "".r
    if (arg.nonEmpty && objects.getObject(arg).nonEmpty){
      val committedFiles = objects.getCommittedFiles(objects.getObject(arg))
      val stagedFiles = index.getAllStagedFiles
      filesToCompare = stagedFiles.filter(f=>committedFiles.contains(f.getFileName))
                                  .map(f=>
                                    (committedFiles(f.getFileName), f.getFile)
                                  )
    } else {

      if(arg.isEmpty){
        fileNameRegex = """(.*) \d (.*)""".r
      } else  if ((currentDir/arg).isRegularFile) {
        fileNameRegex = ("""(.*) \d (""" + Regex.quote((currentDir/arg).toString()) +""")""").r
      } else {
        println(s"fatal: ambiguous argument $arg: unknown revision or path not in the working tree.")
        return Seq()
        // Throw???
      }

      filesToCompare = fileNameRegex.findAllIn(index.indexAsString)
        .matchData
        .map(m => (objects.getObject(m.group(1)).getOrElse(File("")), File(m.group(2)) )).toSeq
    }

    val diff : Seq[(File, Seq[(String, Int, String)])] = for {
      f <- filesToCompare
    }   yield (f._2, diffFiles(f._1, f._2))

    diff

  }

  def diffFiles(a:File, b:File): Seq[(String, Int, String)] ={
    val linesA = a.lines.zipWithIndex.toSeq
    val linesB = b.lines.zipWithIndex.toSeq
    val min = (linesA diff linesB).map(e => ("-", e._2, e._1))
    val add = (linesB diff linesA).map(e => ("+", e._2, e._1))
    val arr = min.concat(add).sortBy(_._2)
    arr
  }

  def printDiff(file: File, diff: Seq[(String, Int, String)]): Unit ={
    println(file)
    diff.foreach(d =>
        if(d._1 == "+"){
          println(Console.GREEN + d._1 + " " + d._2 + " " + d._3 + Console.RESET)
        }else{
          println(Console.RED + d._1 + " " + d._2 + " " + d._3 + Console.RESET)
        }
    )
  }



}
