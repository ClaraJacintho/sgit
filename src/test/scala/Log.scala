import better.files.Dsl._
import better.files.File
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import sgit.Sgit
import utils.Utils

class Log  extends FlatSpec with BeforeAndAfterAll{
  val testDir: File = cwd/"TestLog"
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
  val fileD: File = testDir/"d"


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

  "Log" should "print an error if no commits" in {
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


