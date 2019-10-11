package sgit.objects

import better.files.File

import scala.util.matching.Regex

class StagingArea(index : File) {
  //TODO: Não funciona se tu tenta adicionar de um sub dir!
  // Digamos q o root seja lapin e tenha um subdir toto
  // e tu tenta add um file de toto
  // O problema é q pega currentDir errado => solução? olhar pra
  // dir acima até achar?
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

  def clearStaginArea(): index.type = {
    index.clear()
  }

  def getAllStagedFiles: Seq[Blob] = {
    val fileNameRegex = """(.*) \d (.*)""".r
    fileNameRegex.findAllIn(index.contentAsString)
      .matchData
      .map(m => new Blob(m.group(1), File(m.group(2)) )).toSeq
  }
}
