package utils

object Utils {
  def getFolderFileName(sha: String): Seq[String] ={
    val folderName = sha.substring(0, 2)
    val fileName = sha.substring(2)
    Seq(folderName, fileName)
  }
}
