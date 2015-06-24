package cortex.util

/**
 */
object log {
  private val Blue = "\033[94m"
  private val Green = "\033[92m"
  private val Yellow = "\033[93m"
  private val Red = "\033[91m"
  private val Clear = "\033[0m"
  private val LightPurple = "\033[35m"
  private val Cyan = "\033[36m"

  private def print(color: String)(implicit msg: String) = {
    val stackTrace = Thread.currentThread().getStackTrace
    val caller = stackTrace(5)
    println(
      s"$color[${stackTrace(2).getMethodName}][${caller.getClassName}" +
        s".${caller.getMethodName}:L${caller.getLineNumber}]: $msg$Clear"
    )
  }

  def info(implicit msg: String) = print(Blue)
  def error(implicit msg: String) = print(Red)
  def warn(implicit msg: String) = print(Yellow)
  def trace(implicit msg: String) = print(Green)
  def debug(implicit msg: String) = print(LightPurple)
  def verbose(implicit msg: String) = print(Cyan)

  def i(implicit msg: String) = info
  def e(implicit msg: String) = error
  def w(implicit msg: String) = warn
  def t(implicit msg: String) = trace
  def d(implicit msg: String) = debug
  def v(implicit msg: String) = verbose
}