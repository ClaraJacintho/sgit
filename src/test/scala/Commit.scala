import better.files.Dsl._
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import sgit.Sgit
import utils.Utils

class Commit  extends FlatSpec with BeforeAndAfterAll{
  val testDir = cwd/"ThisProjectSucks"
  val sgit = new Sgit(testDir)
  val sgitDir = testDir/".sgit"
  val index = sgitDir/"index"
  val head = sgitDir/"HEAD"
  val obj = sgitDir/"objects"
  val refs = sgitDir/"refs"
  val headsFolder = refs/"heads"
  val fileA = testDir/"a.txt"
  val testFolderA = testDir/"folderA"
  val testFolderB = testFolderA/"folderB"
  val fileB = testFolderA/"b"
  val fileC = testFolderB/"c"


  override def beforeAll = {
    mkdirs(testDir)
    (testDir/"a.txt").createIfNotExists().appendLine("Existence is pain")
    mkdirs(testFolderA)
    mkdirs(testFolderB)
    fileB.createIfNotExists().appendLine("End it all")
    fileC.createIfNotExists().appendLine("We are born to suffer")
    sgit.init()
    sgit.add(".")
  }

  override def afterAll = {
    testDir.deleteOnExit()
    testDir.delete()
  }

  "Commit" should "create a new blob in objects with the names of all files committed" in {
    sgit.commit("Test commit")
    val commitString = sgit.head.getCurrentCommit.getOrElse("")
    val commitData = Utils.getFolderFileName(commitString)
    val commit = (obj/commitData(0)/commitData(1))
    assert(commit.contentAsString.contains(fileA.toString()))
    assert(commit.contentAsString.contains(fileB.toString()))
    assert(commit.contentAsString.contains(fileC.toString()))
  }
  it should "add the reference to this blob in HEAD" in {
    val commitString = sgit.head.getCurrentCommit.getOrElse("")
    val commitData = Utils.getFolderFileName(commitString)
    val commit = (obj/commitData(0)/commitData(1))
    assert(head.contentAsString.contains(commit.sha1))
  }

}