package sgit.objects

import better.files.File

import scala.util.matching.Regex

class StagingArea(index : File) {

  /***
   * Adds a file to the index (staging area)
   * @param blob
   */
  def addFileToStagingArea(blob: Blob): Unit ={
    val fileNameRegex = ("""(.* [0-3] """+ Regex.quote(blob.getFileName) + """)""" ).r

    if(fileNameRegex.findFirstIn(index.contentAsString).nonEmpty) {
      index.
        overwrite( // is this functional????
          fileNameRegex.replaceAllIn(index.contentAsString, Regex quoteReplacement blob.toStringIndex)
        )
    } else {
      index.appendLine(blob.toStringIndex)
    }
  }

  /***
   * clears the staging area
   */
  def clearStaginArea(): Unit = {
    index.clear()
  }

  /**
   * gets all the staged files
   * @return - all staged files as e sequence of blobs
   */
  def getAllStagedFiles: Seq[Blob] = {
    val fileNameRegex = """(.*) \d (.*)""".r
    fileNameRegex.findAllIn(index.contentAsString)
      .matchData
      .map(m => new Blob(m.group(1), File(m.group(2)) )).toSeq
  }

  def indexAsString: String ={
    index.contentAsString
  }
}
