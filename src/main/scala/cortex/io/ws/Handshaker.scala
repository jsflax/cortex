package cortex.io.ws

import java.io.{InputStreamReader, DataOutputStream, BufferedReader}
import java.net.Socket

import cortex.controller.{WsMessage, Controller, HttpVerb}
import cortex.io.{Input, UndefinedHttpMethod}
import cortex.util.log

import scala.collection.mutable

/**
  * Created by jasonflax on 2/16/16.
  */
private object Handshaker {
  lazy val magicHashString = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
  lazy val sha1 = java.security.MessageDigest.getInstance("SHA-1")
}

private[ws] class Handshaker(socket: Socket) {

  /**
    * Handle the input and write to the output stream,
    * returning data to the client.
    *
    * @param out output stream we are writing to
    */
  @inline private def completeHandshake(socketKey: Option[String],
                                        out: DataOutputStream) = {
    // if successful, write the output following http 1.1 specs
    if (socketKey.isDefined) {
      out.writeBytes("HTTP/1.1 101 Switching Protocols\r\n")

      out.writeBytes("Upgrade: websocket\r\n")
      out.writeBytes("Connection: Upgrade\r\n")
      out.writeBytes(s"Sec-WebSocket-Accept: ${socketKey.get}\r\n")

      out.writeBytes("\r\n")
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
    * @return hand-shook socket key
    */
  @inline
  private def initiateHandshake[A](bufferedReader: BufferedReader):
  (Option[String], Option[WsMessage[A]]) = {
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
      throw new UndefinedHttpMethod(
        s"${line.split(" ")(0)} is not a valid Http method"
      )
    }

    var webSocketKey: String = null
    var headers = mutable.Map[String, String]()

    do {
      line = bufferedReader.readLine()
      log verbose line
      val webSocketKeyKey = "sec-websocket-key: "
      if (line.toLowerCase.startsWith(webSocketKeyKey)) {
        webSocketKey = line.substring(webSocketKeyKey.length)
      } else {
        val header = line.split(":")
        if (header.forall(_.nonEmpty)) {
          headers += header(0) -> header.slice(1, header.length).mkString
        }
      }
    } while (!line.equals(""))

    var queryParameters: String = null

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

      val socketKey = Option(new sun.misc.BASE64Encoder().encode(
        Handshaker.sha1.digest(
          s"$webSocketKey${Handshaker.magicHashString}".getBytes
        )
      ))

      if (socketKey.isDefined) {
        action.get.methods.find(httpMethod.get.equals) match {
          case Some(verb) =>
            socketKey -> Option(
              Input(
                endpoint,
                body = IndexedSeq(),
                queryParameters,
                cookie = None,
                verb,
                headers.toMap,
                action.get,
                action.get.contentType
              ).message.asInstanceOf[WsMessage[A]]
            )
          case None =>
            socketKey -> None
        }
      } else {
        None -> None
      }
    } else {
      log e s"invalid endpoint: $endpoint"
      None -> None
    }
  }

  def shake[A](): (Option[String], Option[WsMessage[A]]) = {
    val keyAndPayloadOpt:
    (Option[String], Option[WsMessage[A]]) =
      initiateHandshake[A](new BufferedReader(
        new InputStreamReader(socket.getInputStream)
      ))

    completeHandshake(
      keyAndPayloadOpt._1,
      new DataOutputStream(socket.getOutputStream)
    )

    keyAndPayloadOpt
  }
}
