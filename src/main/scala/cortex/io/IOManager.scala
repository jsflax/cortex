package cortex.io

import java.io._
import java.net.{Socket, ServerSocket}

import cortex.controller.Controller
import cortex.controller.Controller._
import cortex.controller.Controller.HttpMethod.HttpMethod
import cortex.model._
import cortex.util.log

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Essentially our server, this manages the input and output
 * to and fro the server.
 */
protected class IOManager(port: Int) {

  /**
   * Dumb datum for passing around input information.
   * @param endpoint endpoint being targeting
   * @param body input body for non-GET calls
   * @param queryParams query parameters in URL or in post body
   * @param httpMethod http method being used (GET, POST, etc.)
   * @param action action associated with this endpoint
   * @param contentType accepted content-types
   */
  protected case class Input(endpoint: String,
                           body: IndexedSeq[Byte],
                           queryParams: String,
                           httpMethod: HttpMethod,
                           action: Action[Response],
                           contentType: ContentType.Value)

  /**
   * Handle the input and write to the output stream,
   * returning data to the client.
   * @param input [[Input]] model that we've read from previous method
   * @param out output stream we are writing to
   */
  @inline private def writeOutput(input: Input, out: DataOutputStream) = {
    // check if the http method is registered for this endpoint
    // TODO: yield error message
    if (input.action.methods.contains(input.httpMethod)) {

      // call the handler on the registered action to parse the input
      // and fetch the output (response)
      val response = input.action.handler(
        Response(
          input.queryParams, input.httpMethod, input.body, input.contentType
        )
      )

      // if successful, write the output following http 1.1 specs
      if (response.isDefined) {
        out.writeBytes("HTTP/1.1 200 OK\r\n")
        out.writeBytes("Server: WebServer\r\n")
        out.writeBytes(s"Content-Type: ${input.contentType}\r\n")
        out.writeBytes(s"Content-Length: ${response.get.length}\r\n")
        out.writeBytes("Connection: close\r\n")
        out.writeBytes("\r\n")
        out.write(response.get)
      }
    } else {
      log error
        s"${input.httpMethod} is not a valid Http method for ${input.endpoint}"
    }
  }

  /**
   * Read the input from the input stream.
   * @param bufferedReader input stream
   * @return [[Input]] model (endpoint, body, http method)
   */
  @inline private def readInput(bufferedReader: BufferedReader): Option[Input] = {
    var line: String = null

    // read top line of input
    line = bufferedReader.readLine()
    log info line

    // endpoint will be the second term on this line
    var endpoint = line.split(" ")(1)

    // this line also starts with the http method
    // check our HttpMethods enum for a valid httpMethod
    val httpMethod = HttpMethod.values.collectFirst {
      case method if line startsWith method.toString => method
    }

    // if it is not a valid http method, short circuit
    if (httpMethod.isEmpty) {
      throw new UndefinedHttpMethod(
        s"${line.split(" ")(0)} is not a valid Http method"
      )
    }

    // retrieve content length and content type
    var contentLength = 0
    var contentType = ContentType.NoneType
    do {
      line = bufferedReader.readLine()
      log info line
      if (httpMethod.get != HttpMethod.GET) {
        val contentHeader = "Content-Length: "
        val contentTypeHeader = "Accept: "

        if (line.startsWith(contentHeader)) {
          contentLength = Integer.parseInt(line.substring(contentHeader.length()))
        } else if (line.startsWith(contentTypeHeader)) {
          log warn line
          contentType = ContentType.valueMap.get(
            line.substring(contentTypeHeader.length())
          ).get
        }
      }
    } while (!line.equals(""))

    var queryParameters: String = null
    var body: IndexedSeq[Byte] = null

    // if the endpoint contains query parameters, break them off
    // and split up the endpoint
    if (endpoint.contains("?")) {
      val epSplit = endpoint.split('?')
      endpoint = epSplit(0)
      queryParameters = epSplit(1)
    }

    // get and check that this endpoint is in our registered
    // in one of our controllers
    val action = Controller.actionRegistrants.get(endpoint)

    // if it is defined, read the body and return in the input
    // else, return None, as we aren't going to handle this further
    if (action.isDefined) {
      if (httpMethod.get != HttpMethod.GET) {
        val bodyStream: IndexedSeq[Byte] =
          for (i <- 0 until contentLength)
            yield bufferedReader.read().asInstanceOf[Byte]
        if (bodyStream.size > 0) {
          body = bodyStream
        }
      }
      Option(Input(
        endpoint, body, queryParameters, httpMethod.get, action.get, contentType
      ))
    } else {
      None
    }
  }

  @inline private def ioLoop(implicit socket: Socket) = {
    // retrieve input stream from socket
    val inputStream = new BufferedReader(
      new InputStreamReader(socket.getInputStream)
    )

    // read data from the input stream
    val input = readInput(inputStream)

    // if there were no errors reading the data, write output
    if (input.isDefined) {
      writeOutput(input.get, new DataOutputStream(socket.getOutputStream))
    }

    // close the socket
    socket.close()
  }

  @inline private def socketLoop(implicit server: ServerSocket) = {
    try {
      // system-level wait while we literally wait on a request
      implicit val socket = server.accept()

      // spawn off a new Future once a request has been accepted
      Future {
        ioLoop
      }
    } catch {
      case e: Exception => log error e.getMessage
    }
  }

  def singleTestLoop() = {
    // open new server socket on selected port
    implicit val server = new ServerSocket(port)
    socketLoop
  }

  /**
   * Main IO loop.
   */
  def loop() = {
    // open new server socket on selected port
    implicit val server = new ServerSocket(port)

    // begin main loop
    while (true) {
      socketLoop
    }
  }
}
