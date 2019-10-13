import better.files._
import better.files.Dsl._
import org.scalatest.FlatSpec
import sgit.Sgit

class Test  extends FlatSpec{ // In one suite to run sequentially
  val testDir = mkdirs(cwd/"ThisProjectSucks")
  val sgit = new Sgit(testDir)
  val sgitDir = testDir/".sgit"
  val index = sgitDir/"index"
  val head = sgitDir/"HEAD"
  val obj = sgitDir/"objects"
  val refs = sgitDir/"refs"
  val headsFolder = refs/"heads"

  "Init" should "create an .sgit repo with index and head files and objects and ref folders" in {
    sgit.init()
    assert(sgitDir.isDirectory)
    assert(index.isRegularFile)
    assert(head.isRegularFile)
    assert(obj.isDirectory)
    assert(refs.isDirectory)
  }

  val fileA = (testDir/"a.txt").createIfNotExists()
                               .appendLine("Batatinha quando nasce")
  val fileAData = getFolderFileName(fileA.sha1)
  "Add" should "add a document to the index" in {
    sgit.add("a.txt")

    assert(index.contentAsString.contains(fileA.toString()))
  }
  it should "create an a blob and store it in the object folder" in{
    assert((obj/fileAData(0)).isDirectory)
    assert((obj/fileAData(0)/fileAData(1)).isRegularFile)
  }
  it should "contain the same contents as the original file" in {
    assert( (obj/fileAData(0)/fileAData(1)).contentAsString.contains("Batatinha quando nasce") )
  }
  val testFolderA = mkdirs(testDir/"folderA")
  val testFolderB = mkdirs(testFolderA/"folderB")

  val fileB = (testFolderA/"b").createIfNotExists()
                               .appendLine("Se esparrama pelo chão")
  val fileBData = getFolderFileName(fileB.sha1)

  val fileC = (testFolderB/"c").createIfNotExists().appendLine("")
  val fileCData = getFolderFileName(fileC.sha1)

    it should "when callend in a  folder, add each file to the index" in {
      sgit.add("folderA") //I don't want to relativize sorry :/
      assert(index.contentAsString.contains(fileB.toString()))
      assert(index.contentAsString.contains(fileC.toString()))
    }

    it should "create a blob of each of the files in a folder and its subdirs" in {
      assert((obj/fileBData(0)).isDirectory)
      assert((obj/fileBData(0)/fileBData(1)).isRegularFile)

      assert((obj/fileCData(0)).isDirectory)
      assert((obj/fileCData(0)/fileCData(1)).isRegularFile)
    }

  "Init" should "do nothing if called on a folder with an existing repo" in {
    sgit.init()
    assert(index.contentAsString.contains(fileA.toString()))
    assert((obj/fileAData(0)/fileAData(1)).isRegularFile)

  }

    "Commit" should "create a new blob in objects with the names of all files committed" in {
      sgit.commit("Test commit")
      val commitString = sgit.head.getCurrentCommit.getOrElse("")
      val commitData = getFolderFileName(commitString)
      val commit = (obj/commitData(0)/commitData(1))
      assert(commit.contentAsString.contains(fileA.toString()))
      assert(commit.contentAsString.contains(fileB.toString()))
      assert(commit.contentAsString.contains(fileC.toString()))
    }
    it should "add the reference to this blob in HEAD" in {
      val commitString = sgit.head.getCurrentCommit.getOrElse("")
      val commitData = getFolderFileName(commitString)
      val commit = (obj/commitData(0)/commitData(1))
      assert(head.contentAsString.contains(commit.sha1))
    }

    "Branch" should "create a new file with the branch name in refs" in {
      sgit.branch("foo")
      assert((headsFolder/"foo").isRegularFile)
    }

    it should "contain the last commit" in {
      val commitString = sgit.head.getCurrentCommit.getOrElse("")
      val commitData = getFolderFileName(commitString)
      val commit = (obj/commitData(0)/commitData(1))
      assert((headsFolder/"foo").contentAsString.contains(commit.sha1))
    }
    it should "not create a branch if the name already exists" in {
      assert(!sgit.branch("foo"))
    }

    "In the end this test" should "delete the test folder" in {
      testDir.deleteOnExit()
      testDir.delete()
      assert(!testDir.isDirectory)
    }

    def getFolderFileName(sha: String): Seq[String] ={
      val folderName = sha.substring(0, 2)
      val fileName = sha.substring(2)
      Seq(folderName, fileName)
    }
}
