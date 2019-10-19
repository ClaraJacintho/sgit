import better.files.Dsl._
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import sgit.Sgit
import utils.Utils

class Diff  extends FlatSpec with BeforeAndAfterAll{
  val testDir = cwd/"TestBranch"
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
  val fileD = testDir/"d"


  override def beforeAll = {
    mkdirs(testDir)
    (testDir/"a.txt").createIfNotExists().appendLine("Existence is pain")
    mkdirs(testFolderA)
    mkdirs(testFolderB)
    fileB.createIfNotExists().appendLine("End it all")
    fileC.createIfNotExists().appendLine("We are born to suffer")
    sgit.init()
    sgit.add(".")
    sgit.commit("1")

  }
  override def afterAll = {
    testDir.deleteOnExit()
    testDir.delete()
  }

  "Diff" should "return nothing if there are no changes" in {
   sgit.findDiff("").foreach(d => assert(d._2.isEmpty) )
  }

  it should "show tracked files that were added" in {
    fileD.createIfNotExists().appendLine("Dor e sofrimento")
    sgit.findDiff("").filter(d => d._1 == fileD)
      .foreach(d => assert(d._2.nonEmpty))
  }

  it should "show tracked files that were modified" in {
    fileB.appendLine("AaaAAaaA")
    sgit.findDiff("").filter(d => d._1 == fileB)
      .foreach(d => assert(d._2.nonEmpty))
  }

  it should "show tracked files that were deleted" in {
    fileA.delete()
    sgit.findDiff("").filter(d => d._1 == fileA)
      .foreach(d => assert(d._2.nonEmpty))
  }

  it should "show nothing after committing with all files tracked" in {
    sgit.add(".")
    sgit.commit("test")
    sgit.findDiff("").foreach(d => assert(d._2.isEmpty))
  }
}
