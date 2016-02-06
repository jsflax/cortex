package cortex.util

import scala.language.implicitConversions

/**
 * Internal logger.
 */ //TODO: Connect to offline store for ... logging (duh)
object log {
  var on: Boolean = true

  private val Blue = "\033[94m"
  private val Green = "\033[92m"
  private val Yellow = "\033[93m"
  private val Red = "\033[91m"
  private val Clear = "\033[0m"
  private val LightPurple = "\033[35m"
  private val Cyan = "\033[36m"

  private def print(color: String)(implicit msg: String) = {
    if (on) {
      val stackTrace = Thread.currentThread().getStackTrace
      println(
        s"$color[${stackTrace(2).getMethodName}] $msg$Clear"
      )
    }
  }

  implicit def anyToString(any: Any): String = any.toString

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