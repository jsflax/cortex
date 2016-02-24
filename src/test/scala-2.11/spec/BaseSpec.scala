package spec

import java.net.InetAddress
import java.util.concurrent.Executors

import cortex.app.Cortex
import cortex.controller.Controller
import cortex.io.{IOManager, HttpProtocolManager}
import cortex.util.log
import cortex.view.View
import org.scalatest._

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by jasonflax on 2/6/16.
  */

private object BaseSpec {

  private lazy val _tPort: mutable.Queue[Int] =
    mutable.Queue(Range(8081, 47000): _*)

  private def nextPort = synchronized {
    val port = _tPort.dequeue()
    _tPort.enqueue(port)
    port
  }
}

trait BaseSpec
  extends FlatSpec
    with Matchers
    with BeforeAndAfterAll
    with Cortex {

  log.setLogLevels(log.Error, log.Debug)

  val localhost = InetAddress.getLocalHost.getHostAddress

  val _port = BaseSpec.nextPort

  def port = _port

  override def ioManagers: Seq[_ <: IOManager] = Seq(
    new HttpProtocolManager(
      port,
      executionContext = new ExecutionContext {
        val threadPool = Executors.newFixedThreadPool(1000)

        def execute(runnable: Runnable) {
          threadPool.submit(runnable)
        }

        def reportFailure(t: Throwable) {}
      }
    )
  )

  import scala.concurrent.duration._

  def async[T](operation: => T): T = {
    val future = Future {
      operation
    }

    Await.result(future, 2000.millis)
  }

  override def start() =
    ioManagers.foreach(io => io.loop())

  override def beforeAll = {
    log trace s"Starting $suiteName on port $port"
    start()
  }
}
