import better.files.Dsl._
import better.files.File
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import sgit.Sgit
import utils.Utils

class Status  extends FlatSpec with BeforeAndAfterAll{
  val testDir: File = cwd/"TestStatus"
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
    sgit.init()
  }
  override def afterAll: Unit = {
    testDir.deleteOnExit()
    testDir.delete()
  }

  "Status" should "print  no files to commit if there have been no changes" in {
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {
      sgit.status()
      assert(stream.toString.contains("No changes to commit"))
    }
  }

  it should "print untracked files when they are added" in {
    (testDir/"a.txt").createIfNotExists().appendLine("Existence is pain")
    mkdirs(testFolderA)
    mkdirs(testFolderB)
    fileB.createIfNotExists().appendLine("End it all")
    fileC.createIfNotExists().appendLine("We are born to suffer")
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {
      //all printlns in this block will be redirected
      sgit.status()
      assert(stream.toString().contains(testDir.relativize(fileA).toString))
      assert(stream.toString().contains(testDir.relativize(fileB).toString))
      assert(stream.toString().contains(testDir.relativize(fileC).toString))
    }
  }

  it should "print nothing again after a commit" in {
    sgit.add(".")
    sgit.commit("this is the second commit")
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {
      //all printlns in this block will be redirected
      sgit.status()
      assert(stream.toString().contains("No changes to commit"))
    }
  }

  it should "show tracked files that have been modified" in {
    fileB.append("aaaa")
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {
      //all printlns in this block will be redirected
      sgit.status()
      assert(!stream.toString().contains(testDir.relativize(fileA).toString))
      assert(stream.toString().contains(testDir.relativize(fileB).toString))
      assert(!stream.toString().contains(testDir.relativize(fileC).toString))
    }
    sgit.add(".")
    sgit.commit("test")
  }

  it should "show tracked files that have been deleted" in {
   fileC.delete()
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {
      //all printlns in this block will be redirected
      sgit.status()
      assert(!stream.toString().contains(testDir.relativize(fileA).toString))
      assert(!stream.toString().contains(testDir.relativize(fileB).toString))
      assert(stream.toString().contains(testDir.relativize(fileC).toString))
    }
    sgit.commit("test")
  }

  it should "show tracked files that have been modified and will be committed" in {
    fileB.append("aaaa")
    sgit.add(".")
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {
      //all printlns in this block will be redirected
      sgit.status()
      assert(!stream.toString().contains(testDir.relativize(fileA).toString))
      assert(stream.toString().contains(testDir.relativize(fileB).toString))
    }
    sgit.commit("test")
  }

  it should "show tracked files that have been deleted  and will be committed" in {
    fileB.delete()
    sgit.add(".")
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {
      //all printlns in this block will be redirected
      sgit.status()
      assert(!stream.toString().contains(testDir.relativize(fileA).toString))
      assert(stream.toString().contains(testDir.relativize(fileB).toString))
    }
    sgit.commit("test")
  }

  it should "show tracked files that have been added  and will be committed" in {
   (testDir/"d.txt").createIfNotExists().append("hewwo")
    sgit.add(".")
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {
      //all printlns in this block will be redirected
      sgit.status()
      assert(!stream.toString().contains(testDir.relativize(fileA).toString))
      assert(!stream.toString().contains(testDir.relativize(fileB).toString))
      assert(stream.toString().contains("d.txt"))

    }
  }

}


