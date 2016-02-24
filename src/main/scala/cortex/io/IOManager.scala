package cortex.io

import java.io._
import java.net.{InetSocketAddress, ServerSocket, Socket}

import cortex.util.log

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

/**
  * Essentially our server, this manages the input and output
  * to and fro the server.
  */
abstract class IOManager(port: Int,
                         executionContext: ExecutionContext = global) {
  implicit val _executionContext = executionContext

  // open new server socket on selected port
  lazy val server = {
    val ss = new ServerSocket()
    ss.setReuseAddress(true)

    ss.bind(new InetSocketAddress(port))
    ss
  }

  @volatile var isRunning = false

  protected def ioLoop(socket: Socket)

  @inline private def socketLoop(server: ServerSocket) = {
    var socket: Socket = null

    try {
      log trace "Awaiting request"
      // system-level wait while we literally wait on a request
      socket = server.accept()

      log trace "Socket accepted"
      // spawn off a new Future once a request has been accepted
      Future {
        ioLoop(socket)
      }
    } catch {
      case e: Exception => log error e.getMessage; socket.close()
    }
  }

  def shutdown() = {
    isRunning = false
    try {
      server.close()
    } catch {
      case e: IOException => e.printStackTrace()
    }
  }

  /**
    * Main IO loop.
    */
  def loop() = {
    isRunning = true
    Future {
      // begin main loop
      while (isRunning) {
        socketLoop(server)
      }
    }
  }
}
