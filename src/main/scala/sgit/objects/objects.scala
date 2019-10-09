package sgit.objects

class Blob(sha : String, fileName: String){
   def toStringIndex: String = {
    s"$sha 0 $fileName"
  }

  def toStringCommit: String ={
    s"blob $sha $fileName"
  }
}
class Tree{

}

class commit {

}
