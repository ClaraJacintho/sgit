import better.files._
import better.files.Dsl._

import org.scalatest.FunSuite
class Test  extends FunSuite{
    test("sgit.init"){
      val gitPath = cwd/".sgit"
      SGIT.init()
      assert(!gitPath.isEmpty)
    }

    test("sgit.add.file"){
      val file = cwd/"foo.txt"
      file.createIfNotExists().append("Hello World")
      SGIT.add("foo.txt")
    }
}
