import better.files.Dsl._
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import sgit.Sgit
import utils.Utils

class Checkout  extends FlatSpec with BeforeAndAfterAll{
  val testDir = cwd/"TestCheckout"
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
  val fileD = testDir/"dee"


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
    sgit.branch("foo")
    fileD.createIfNotExists().appendLine("Dor e sofrimento")
    sgit.add(".")
    sgit.commit("2")
  }

  override def afterAll = {
    testDir.deleteOnExit()
    testDir.delete()
  }


  "Checkout" should "do nothing if the target branch does not exist" in {
    assert(!sgit.checkout("aaaaaaaaaaaaaaaaaaaaaaaaaa"))
  }

  it should "do nothing if the target branch is the same as the current branch" in {
    assert(!sgit.checkout("foo"))
  }

  it should "delete committed files not present in the target branch" in {
    assert(sgit.checkout("master"))
    assert(!fileD.exists)
  }



  it should "do nothing if there are uncommitted changes to tracked files" in {
    fileB.appendLine("AaaAAaaA")
    sgit.add(".")
    assert(!sgit.checkout("foo"))
  }


}
