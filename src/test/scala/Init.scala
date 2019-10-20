import better.files._
import better.files.Dsl._
import org.scalatest.FlatSpec
import sgit.Sgit

class Init  extends FlatSpec{ // In one suite to run sequentially
  val testDir: File = mkdirs(cwd/"TestInit")
  val sgit = new Sgit(testDir)
  val sgitDir: File = testDir/".sgit"
  val index: File = sgitDir/"index"
  val head: File = sgitDir/"HEAD"
  val obj: File = sgitDir/"objects"
  val refs: File = sgitDir/"refs"
  val headsFolder: File = refs/"heads"

  "Init" should "create an .sgit repo with index and head files and objects and ref folders" in {
    sgit.init()
    assert(sgitDir.isDirectory)
    assert(index.isRegularFile)
    assert(head.isRegularFile)
    assert(obj.isDirectory)
    assert(refs.isDirectory)
  }

  //
}
