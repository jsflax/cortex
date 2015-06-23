package cortex.io

import java.io._
import java.net.ServerSocket

import cortex.controller.Controller
import cortex.controller.Controller._
import cortex.controller.Controller.HttpMethod.HttpMethod
import cortex.controller.model._
import cortex.util.log

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 */
protected class IOManager(port: Int) {
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
  @inline def writeOutput(input: Input, out: DataOutputStream) = {
    if (input.action.methods.contains(input.httpMethod)) {
      val response = input.action.handler(
        Response(
          input.queryParams, input.httpMethod, input.body, input.contentType
        )
      )
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
  @inline def readInput(bufferedReader: BufferedReader): Option[Input] = {
    var line: String = null
    line = bufferedReader.readLine()
    log trace line
    var endpoint = line.split(" ")(1)

    val httpMethod = HttpMethod.values.collectFirst {
      case method if line startsWith method.toString => method
    }

    if (httpMethod.isEmpty) {
      throw new UndefinedHttpMethod(
        s"${line.split(" ")(0)} is not a valid Http method"
      )
    }

    var contentLength = 0
    var contentType = ContentType.NoneType
    do {
      line = bufferedReader.readLine()
      log trace line
      if (httpMethod.get != HttpMethod.GET) {
        val contentHeader = "Content-Length: "
        val contentTypeHeader = "Accept: "

        if (line.startsWith(contentHeader)) {
          contentLength = Integer.parseInt(line.substring(contentHeader.length()))
        } else if (line.startsWith(contentTypeHeader)) {
          contentType = ContentType.valueMap.get(
            line.substring(contentTypeHeader.length())
          ).get
        }
      }
    } while (!line.equals(""))


    var queryParameters: String = null
    var body: IndexedSeq[Byte] = null

    if (endpoint.contains("?")) {
      val epSplit = endpoint.split('?')
      endpoint = epSplit(0)
      queryParameters = epSplit(1)
    }

    val action = Controller.actionRegistrants.get(endpoint)

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

  /**
   * Main IO loop.
   */
  def loop() = {
    val server = new ServerSocket(port)
    while (true) {
      try {
        val socket = server.accept()
        Future {
          val inputStream = new BufferedReader(
            new InputStreamReader(socket.getInputStream)
          )
          val input = readInput(inputStream)
          if (input.isDefined) {
            writeOutput(input.get, new DataOutputStream(socket.getOutputStream))
          }
          socket.close()
        }
      } catch {
        case e: Exception => log error e.getMessage
      }
    }
  }
}
