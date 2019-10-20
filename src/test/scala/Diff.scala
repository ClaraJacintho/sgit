import better.files.Dsl._
import better.files.File
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import sgit.Sgit
import utils.Utils

class Diff  extends FlatSpec with BeforeAndAfterAll{
  val testDir: File = cwd/"TestDiff"
  val sgit = new Sgit(testDir)
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
  val fileD: File = testDir/"d"


  override def beforeAll: Unit = {
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
  override def afterAll: Unit = {
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
