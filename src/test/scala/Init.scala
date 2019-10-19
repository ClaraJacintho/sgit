import better.files._
import better.files.Dsl._
import org.scalatest.FlatSpec
import sgit.Sgit

class Init  extends FlatSpec{ // In one suite to run sequentially
  val testDir = mkdirs(cwd/"ThisProjectSucks")
  val sgit = new Sgit(testDir)
  val sgitDir = testDir/".sgit"
  val index = sgitDir/"index"
  val head = sgitDir/"HEAD"
  val obj = sgitDir/"objects"
  val refs = sgitDir/"refs"
  val headsFolder = refs/"heads"

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
