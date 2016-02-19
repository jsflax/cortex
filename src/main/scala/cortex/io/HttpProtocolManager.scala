package cortex.io

import java.io.{DataOutputStream, InputStreamReader, BufferedReader}
import java.net.Socket

import cortex.controller.ContentType._
import cortex.controller.Controller.Message
import cortex.controller.{HttpVerb, Controller, ContentType}
import cortex.util.log

import scala.collection.mutable
import scala.concurrent.ExecutionContext

/**
  * Created by jasonflax on 2/13/16.
  */
class HttpProtocolManager(port: Int,
                          executionContext: ExecutionContext = ExecutionContext.global)
  extends IOManager(port: Int, executionContext) {

  /**
    * Handle the input and write to the output stream,
    * returning data to the client.
    *
    * @param out output stream we are writing to
    */
  @inline protected def writeOutput(message: Message,
                                    contentType: ContentType,
                                    out: DataOutputStream,
                                    closed: Boolean = true) = {
    // if successful, write the output following http 1.1 specs
    if (message.response.isDefined) {
      if (message.redirect.isDefined) {
        out.writeBytes("HTTP/1.1 302 Found\r\n")
        out.writeBytes(s"Location: ${message.redirect.get}\r\n")
      } else {
        out.writeBytes("HTTP/1.1 200 OK\r\n")
      }
      if (message.cookie.isDefined) {
        out.writeBytes(s"Set-Cookie: ${message.cookie.get}\r\n")
      }
      out.writeBytes("Server: WebServer\r\n")
      out.writeBytes(s"Content-Type: $contentType\r\n")
      out.writeBytes(s"Content-Length: ${message.response.get.length}\r\n")

      if (closed) {
        out.writeBytes("Connection: close\r\n")
      } else {
        out.writeBytes("Connection: Keep-Alive\r\n")
      }

      out.writeBytes("\r\n")
      out.write(message.response.get)
    } else {
      out.writeBytes("HTTP/1.1 400 Bad request\r\n")
      out.writeBytes("Server: WebServer\r\n")
      out.writeBytes("Connection: close\r\n")
      out.writeBytes("\r\n")
    }
  }

  /**
    * Read the input from the input stream.
    *
    * @param bufferedReader input stream
    * @return [[Input]] model (endpoint, body, http method)
    */
  @inline protected def readInput(bufferedReader: BufferedReader): Option[Input] = {
    var line: String = null

    // read top line of input
    line = bufferedReader.readLine()
    log info line

    // endpoint will be the second term on this line
    var endpoint = line.split(" ")(1).trim

    // this line also starts with the http method
    // check our HttpMethods enum for a valid httpMethod
    val httpMethod = HttpVerb.values.collectFirst {
      case method if line startsWith method.toString => method
    }

    // if it is not a valid http method, short circuit
    if (httpMethod.isEmpty) {
      log v s"${line.split(" ")(0)} is not a valid Http method"
      throw new UndefinedHttpMethod(
        s"${line.split(" ")(0)} is not a valid Http method"
      )
    }

    // retrieve content length and content type
    var contentLength = 0
    var contentType = ContentType.NoneType
    var cookie = Option.empty[String]

    var headers = mutable.Map[String, String]()

    do {
      line = bufferedReader.readLine()
      log verbose line
      if (httpMethod.get != HttpVerb.GET) {
        val contentHeader = "content-length: "
        val contentTypeHeader = "content-type: "
        val cookieHeader = "cookie: "
        if (line.toLowerCase.startsWith(contentHeader)) {
          contentLength = Integer.parseInt(line.substring(contentHeader.length()))
        } else if (line.toLowerCase.startsWith(contentTypeHeader)) {
          val types = line.substring(contentTypeHeader.length()).trim.split(';')
          contentType =
            ContentType.valueMap.filterKeys(types.contains(_)).values.head
        } else if (line.toLowerCase.startsWith(cookieHeader)) {
          cookie = Option(line.substring(cookieHeader.length()))
        } else {
          val header = line.split(":")
          if (header.forall(_.nonEmpty)) {
            headers += header(0) -> header.slice(1, header.length).mkString
          }
        }
      }
    } while (!line.equals(""))

    log.v(s"headers: $headers")

    var queryParameters: String = null
    var body: IndexedSeq[Byte] = null

    // if the endpoint contains query parameters, break them off
    // and split up the endpoint
    if (endpoint.contains("?")) {
      val epSplit = endpoint.split('?')
      endpoint = epSplit(0)
      queryParameters = epSplit(1)
    }

    log.v(endpoint)

    // get and check that this endpoint is in our registered
    // in one of our controllers
    val action = Controller.actionRegistrants.collectFirst {
      case ctx if endpoint.matches(ctx.actionContext.regex.toString()) => ctx
    }

    // if it is defined, read the body and return in the input
    // else, return None, as we aren't going to handle this further
    if (action.isDefined) {
      log v action.get.actionContext.endpoint

      if (httpMethod.get != HttpVerb.GET) {
        val bodyStream: IndexedSeq[Byte] =
          for (i <- 0 until contentLength)
            yield bufferedReader.read().asInstanceOf[Byte]
        if (bodyStream.nonEmpty) {
          body = bodyStream
        }
      }

      action.get.methods.find(httpMethod.get.equals) match {
        case Some(verb) =>
          Option(
            Input(
              endpoint,
              body,
              queryParameters,
              cookie,
              verb,
              headers.toMap,
              action.get,
              contentType
            )
          )
        case None => None
      }
    } else {
      log e s"invalid endpoint: $endpoint"
      None
    }
  }

  override def ioLoop(socket: Socket) = {
    log trace "Reading Input"
    // retrieve input stream from socket
    val inputStream = new BufferedReader(
      new InputStreamReader(socket.getInputStream)
    )

    log trace "Done reading input"

    // read data from the input stream
    val input = readInput(inputStream)

    // if there were no errors reading the data, write output
    if (input.isDefined) {
      writeOutput(
        input.get.message,
        input.get.contentType,
        new DataOutputStream(socket.getOutputStream))
    }

    log trace "Done writing output"

    // close the socket
    socket.close()
  }
}
