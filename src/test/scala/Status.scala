import better.files.Dsl._
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import sgit.Sgit
import utils.Utils

class Status  extends FlatSpec with BeforeAndAfterAll{
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
  }
  override def afterAll = {
    testDir.deleteOnExit()
    testDir.delete()
  }

  "Status" should "print an error if no commits" in {
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {
      sgit.log()
      assert(stream.toString.contains("your current branch does not have any commits yet"))
    }
  }

  it should "print out one commit if there is only " in {
    sgit.add(".")
    sgit.commit("this is the first commit")
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {
      //all printlns in this block will be redirected
      sgit.log()
      assert(stream.toString().contains("this is the first commit"))
    }
  }

  it should "print out all commits if there are more than one" in {
    fileA.append("A change")
    sgit.add(".")
    sgit.commit("this is the second commit")
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {
      //all printlns in this block will be redirected
      sgit.log()
      assert(stream.toString().contains("this is the first commit"))
      assert(stream.toString().contains("this is the second commit"))
    }
  }
}


