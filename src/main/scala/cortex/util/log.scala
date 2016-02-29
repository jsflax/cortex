package cortex.util

import scala.collection.mutable
import scala.language.implicitConversions

/**
 * Internal logger.
 */ //TODO: Connect to offline store for ... logging (duh)
object log {
  private val Blue = "\033[94m"
  private val Green = "\033[92m"
  private val Yellow = "\033[93m"
  private val Red = "\033[91m"
  private val Clear = "\033[0m"
  private val LightPurple = "\033[35m"
  private val Cyan = "\033[36m"

  val Info = 0
  private val _info = (msg: String) => print(msg, Blue)

  val Error = 1
  private val _error = (msg: String) => print(msg, Red)

  val Warn = 2
  private val _warn = (msg: String) => print(msg, Yellow)

  val Trace = 3
  private val _trace = (msg: String) => print(msg, Green)

  val Debug = 4
  private val _debug = (msg: String) => print(msg, LightPurple)

  val Verbose = 5
  private val _verbose = (msg: String) => print(msg, Cyan)

  var on: Boolean = true

  private val functionSet: Map[Int, String => Unit] = Map(
    Info -> _info,
    Error -> _error,
    Warn -> _warn,
    Trace -> _trace,
    Debug -> _debug,
    Verbose -> _verbose
  )

  private val filters = mutable.Map() ++ functionSet

  def setLogLevels(levels: Int*) = {
    val empty = (msg: String) => {}
    val filterAndFilterNot = (0 to 5).partition(levels.contains)
    filterAndFilterNot._1.foreach(i => filters += (i -> functionSet(i)))
    filterAndFilterNot._2.foreach(i => filters += (i -> empty))
  }

  private def print(msg: String, color: String) = {
    if (on) {
      try {
        val stackTrace = Thread.currentThread().getStackTrace
        println(
          s"$color[${stackTrace(4).getMethodName}] $msg$Clear"
        )
      } catch {
        case e: Exception =>
          println(
            s"$color[log] $msg$Clear"
          )
      }
    }
  }

  def info(implicit msg: String) = filters(Info)(msg)
  def error(implicit msg: String) = filters(Error)(msg)
  def warn(implicit msg: String) = filters(Warn)(msg)
  def trace(implicit msg: String) = filters(Trace)(msg)
  def debug(implicit msg: String) = filters(Debug)(msg)
  def verbose(implicit msg: String) = filters(Verbose)(msg)

  def i(implicit msg: String) = info
  def e(implicit msg: String) = error
  def w(implicit msg: String) = warn
  def t(implicit msg: String) = trace
  def d(implicit msg: String) = debug
  def v(implicit msg: String) = verbose
}
