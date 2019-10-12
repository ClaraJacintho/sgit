package sgit

import better.files.Dsl.{cwd, mkdir, mkdirs}
import better.files.File
import sgit.objects
import sgit.objects.{Blob, Commit, Head, StagingArea, Tree}

import scala.util.matching.Regex

class Sgit(currentDir : File) {
  val gitPath: File = currentDir/".sgit"
  val index: StagingArea = new StagingArea(gitPath/"index")
  val head: Head = new Head(gitPath/"HEAD", gitPath/"refs")

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

  /**
   * Adds a file to the index and creates a copy on the objects folder
   * @param file - the file that will be added
   */
  def addBlob(file: File): Unit ={
      val sha = file.sha1
      val blob:Blob = new Blob(sha, file)
      addToObjects(file,sha)
      index.addFileToStagingArea(blob)
  }

  /**
   * Adds a file to the objects folder
   * @param file - the file to be added (file, tree, commit)
   * @param sha - the sha1 string
   */
  def addToObjects(file: File, sha: String): Unit ={
    val folder = gitPath / "objects" / sha.substring(0, 2)
    val fileName = sha.substring(2)
    if (!(folder / fileName).isRegularFile) {
      if (!folder.isDirectory)
        mkdir(folder)
      file.copyToDirectory(folder).renameTo(fileName) //zip?
    }
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
      }*/

    File.usingTemporaryFile() {tempFile =>
      tempFile.appendLine(message)
      stagedFiles.foreach(file => tempFile.appendLine(file.toStringIndex))
      val sha = tempFile.sha1
      addToObjects(tempFile,sha)
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
        tempFile.appendLine(blob.toStringTree)
      )

      val trees: Seq[Tree] = for{
        dir <- allFoldersInDir
      } yield createTree(dir, stagedFolders, stagedFiles.filter(blob =>blob.getFile.isChildOf(dir)))

      trees.distinct.foreach(t => tempFile.appendLine(t.toStringTree))
      val sha = tempFile.sha1
      resultingTree = new Tree(sha, folder)
      addToObjects(tempFile,sha)
    }
    resultingTree
  }

  def branch(name:String): Unit ={
        head.createBranch(name)
  }

  def checkout(branch: String): Unit ={
    // see if branch exists
    // get last commit on target branch
    // copy files back (overwrite current versions if necessary (how?)
  }

  def status(): Unit ={
    val stagedFiles = index.getAllStagedFiles
    println("staged files")
    stagedFiles.foreach(println)

    println("untracked")
    val untrackedFiles: Unit = currentDir.listRecursively
                                  .filter(_ != gitPath)
                                  .filter(!_.isChildOf(gitPath))
                                  .filter(!_.isDirectory)
                                  .filter(!stagedFiles.contains(_))
                                  .foreach(println)

  }
}
