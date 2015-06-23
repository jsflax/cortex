package cortex.util

/**
 */
object log {
  private val Header = "\033[95m"
  private val Blue = "\033[94m"
  private val Green = "\033[92m"
  private val Yellow = "\033[93m"
  private val Red = "\033[91m"
  private val Clear = "\033[0m"
  private val Bold = "\033[1m"
  private val Underline = "\033[4m"


  private def print(color: String)(implicit msg: String) = {
    val stackTrace = Thread.currentThread().getStackTrace
    val caller = stackTrace(5)
    println(
      s"$color[${stackTrace(2).getMethodName}][${caller.getClassName}.${caller.getMethodName}:L${caller.getLineNumber}]: $msg$Clear"
    )
  }

  def info(implicit msg: String) = print(Blue)
  def error(implicit msg: String) = print(Red)
  def warn(implicit msg: String) = print(Yellow)
  def trace(implicit msg: String) = print(Green)
}