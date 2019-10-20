import better.files.Dsl._
import better.files.File
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import sgit.Sgit
import utils.Utils

class Commit  extends FlatSpec with BeforeAndAfterAll{
  val testDir: File = cwd/"TestCommit"
  val sgit: Sgit = new Sgit(testDir)
  val sgitDir: File = testDir/".sgit"
  val index: File = sgitDir/"index"
  val head: File = sgitDir/"HEAD"
  val obj: File = sgitDir/"objects"
  val refs: File = sgitDir/"refs"
  val headsFolder: File = refs/"heads"
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
    sgit.add(".")
  }

  override def afterAll: Unit = {
    testDir.deleteOnExit()
    testDir.delete()
  }

  "Commit" should "create a new blob in objects with the names of all files committed" in {
    sgit.commit("Test commit")
    val commitString = sgit.head.getCurrentCommit.getOrElse("")
    val commitData = Utils.getFolderFileName(commitString)
    val commit = obj/commitData(0)/commitData(1)
    assert(commit.contentAsString.contains(fileA.toString()))
    assert(commit.contentAsString.contains(fileB.toString()))
    assert(commit.contentAsString.contains(fileC.toString()))
  }
  it should "add the reference to this blob in HEAD" in {
    val commitString = sgit.head.getCurrentCommit.getOrElse("")
    val commitData = Utils.getFolderFileName(commitString)
    val commit = obj/commitData(0)/commitData(1)
    assert(head.contentAsString.contains(commit.sha1))
  }

}