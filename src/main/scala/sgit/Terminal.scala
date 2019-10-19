package sgit

object Terminal {
  /***
   * Prints a messege on the console with a given color
   * @param message - the message to be committed
   * @param color - the color of the message, defaults to none
   */
  def log(message: String, color: String = Console.RESET) = {
      println(color + message + Console.RESET)
  }
}
