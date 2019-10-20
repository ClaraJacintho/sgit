import better.files.Dsl._
import better.files.File
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import sgit.Sgit
import utils.Utils

class Branch  extends FlatSpec with BeforeAndAfterAll{
    val testDir: File = cwd/"TestBranch"
    val sgit: Sgit = new Sgit(testDir)
    val sgitDir: File = testDir/".sgit"
    val index: File = sgitDir/"index"
    val head: File = sgitDir/"HEAD"
    val obj: File = sgitDir/"objects"
    val refs: File = sgitDir/"refs"
    val headsFolder: File = refs/"heads"
    val tagsFolder: File = refs/"tags"
    val fileA: File = testDir/"a.txt"
    val testFolderA: File = testDir/"folderA"
    val testFolderB: File = testFolderA/"folderB"
    val fileB: File = testFolderA/"b"
    val fileC: File = testFolderB/"c"


    override def beforeAll: Unit = {
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
    override def afterAll: Unit = {
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
        val commit = obj/commitData(0)/commitData(1)
        assert((headsFolder/"foo").contentAsString.contains(commit.sha1))
    }
    it should "not create a branch if the name already exists" in {
        assert(!sgit.branch("foo"))
    }

    "Tag" should "create a new file with the teg name in refs" in {
        sgit.tag("foo")
        assert((tagsFolder/"foo").isRegularFile)
    }
    it should "not create a tag if the name already exists" in {
        assert(!sgit.tag("foo"))
    }
}
