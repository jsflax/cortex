package cortex.io

import java.io._
import java.net.{InetSocketAddress, Socket, ServerSocket}

import cortex.controller.ContentType.ContentType
import cortex.controller.{Controller, ContentType, HttpMethod}
import cortex.controller.Controller._
import cortex.controller.HttpMethod.HttpMethod
import cortex.model._
import cortex.util.log

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.global

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

  /**
    * Dumb datum for passing around input information.
    *
    * @param endpoint    endpoint being targeting
    * @param body        input body for non-GET calls
    * @param queryParams query parameters in URL or in post body
    * @param httpMethod  http method being used (GET, POST, etc.)
    * @param action      action associated with this endpoint
    * @param contentType accepted content-types
    */
  protected case class Input(endpoint: String,
                             body: IndexedSeq[Byte],
                             queryParams: String,
                             cookie: Option[String],
                             httpMethod: HttpMethod,
                             action: Action,
                             contentType: ContentType) {
    // call the handler on the registered action to parse the input
    // and fetch the output (response)
    lazy val message = action.handler(
      Request(
        queryParams = queryParams,
        httpMethod = httpMethod,
        entity = body,
        extractedParams = action.actionContext.map(endpoint),
        contentType = contentType,
        cookie = cookie
      )
    )
  }

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
