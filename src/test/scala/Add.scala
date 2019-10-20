import better.files.Dsl.{cwd, mkdirs}
import better.files.File
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import sgit.Sgit
import utils.Utils

class Add extends FlatSpec with BeforeAndAfterAll {
  val testDir: File = cwd/"AddTest"
  val sgit: Sgit = new Sgit(testDir)
  val sgitDir: File = testDir/".sgit"
  val index: File = sgitDir/"index"
  val obj: File = sgitDir/"objects"
  val fileA: File = testDir/"a.txt"
  val testFolderA: File = testDir/"folderA"
  val testFolderB: File = testFolderA/"folderB"
  val fileB: File = testFolderA/"b"
  val fileC: File = testFolderB/"c"


  override def beforeAll: Unit = {
    mkdirs(testDir)
    (testDir/"a.txt").createIfNotExists().appendLine("Existence is pain")
    mkdirs(testFolderA)
    mkdirs(testFolderB)
    fileB.createIfNotExists().appendLine("End it all")
    fileC.createIfNotExists().appendLine("We are born to suffer")
    sgit.init()
  }

  override def afterAll: Unit = {
    testDir.deleteOnExit()
    testDir.delete()
  }



  "Add" should "add a document to the index" in {
    sgit.add("a.txt")
    assert(index.contentAsString.contains(fileA.toString()))
  }

  it should "create an a blob and store it in the object folder" in{
    val fileAData = Utils.getFolderFileName(fileA.sha1)
    println(obj/fileAData(0), fileAData)
    assert((obj/fileAData(0)).isDirectory)
    assert((obj/fileAData(0)/fileAData(1)).isRegularFile)
  }
  it should "the blob should contain the same contents as the original file" in {
    val fileAData = Utils.getFolderFileName(fileA.sha1)
    assert( (obj/fileAData(0)/fileAData(1)).contentAsString.contains("Existence is pain") )
  }

  it should "when called in a  folder, add each file to the index" in {
    sgit.add("folderA")
    assert(index.contentAsString.contains(fileB.toString()))
    assert(index.contentAsString.contains(fileC.toString()))
  }

  it should "create a blob of each of the files in a folder and its subdirs" in {
    val fileCData = Utils.getFolderFileName(fileC.sha1)
    val fileBData = Utils.getFolderFileName(fileB.sha1)

    assert((obj/fileBData(0)).isDirectory)
    assert((obj/fileBData(0)/fileBData(1)).isRegularFile)

    assert((obj/fileCData(0)).isDirectory)
    assert((obj/fileCData(0)/fileCData(1)).isRegularFile)
  }


}


