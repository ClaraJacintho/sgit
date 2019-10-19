import better.files.Dsl._
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import sgit.Sgit
import utils.Utils

class Branch  extends FlatSpec with BeforeAndAfterAll{
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


    override def beforeAll = {
        mkdirs(testDir)
        (testDir/"a.txt").createIfNotExists().appendLine("Existence is pain")
        mkdirs(testFolderA)
        mkdirs(testFolderB)
        fileB.createIfNotExists().appendLine("End it all")
        fileC.createIfNotExists().appendLine("We are born to suffer")
        sgit.init()
        sgit.add(".")
        sgit.commit("hello")
    }
    override def afterAll = {
        testDir.deleteOnExit()
        testDir.delete()
    }

    "Branch" should "create a new file with the branch name in refs" in {
        sgit.branch("foo")
        assert((headsFolder/"foo").isRegularFile)
    }

    it should "contain the last commit" in {
        val commitString = sgit.head.getCurrentCommit.getOrElse("")
        val commitData = Utils.getFolderFileName(commitString)
        val commit = (obj/commitData(0)/commitData(1))
        assert((headsFolder/"foo").contentAsString.contains(commit.sha1))
    }
    it should "not create a branch if the name already exists" in {
        assert(!sgit.branch("foo"))
    }
}
