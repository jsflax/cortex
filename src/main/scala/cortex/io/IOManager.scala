package cortex.io

import java.io._
import java.net.{InetSocketAddress, Socket, ServerSocket}

import cortex.controller.ContentType.ContentType
import cortex.controller.Controller._
import cortex.controller.HttpVerb
import cortex.model._
import cortex.util.log
import spray.json.{JsBoolean, JsString, JsObject}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.global
import scala.util.{Failure, Success, Try}

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
                             httpMethod: HttpVerb[_ <: Primitive[_]],
                             headers: Map[String, String],
                             action: Action,
                             contentType: ContentType) {
    // call the handler on the registered action to parse the input
    // and fetch the output (response)
    lazy val message: Message =
      Try(
        action.handler(
          Request(
            queryParams = queryParams,
            verb = httpMethod,
            headers = headers,
            entity = body,
            extractedParams = action.actionContext.map(endpoint),
            contentType = contentType,
            cookie = cookie
          )
        )
      ) match {
        case Success(msg) => msg
        case Failure(msg) =>
          log e s"Internal server error: ${msg.getMessage}"
          msg.printStackTrace()

          Message(
            Option(
              JsObject(
                "success" -> JsBoolean(false),
                "error" -> JsString("Internal server error")
              ).toString().getBytes
            ),
            cookie,
            None
          )
      }
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
