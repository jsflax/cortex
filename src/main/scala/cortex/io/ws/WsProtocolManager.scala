package cortex.io.ws

import java.net._

import cortex.controller.WsController
import cortex.io.IOManager

import scala.concurrent.ExecutionContext._
import scala.concurrent.{ExecutionContext, Future}

import scala.language.implicitConversions

/**
  * Created by jasonflax on 2/12/16.
  */
abstract class WsProtocolManager[A](port: Int,
                                 executionContext: ExecutionContext = global)
  extends IOManager(port, executionContext) with WsController {

  implicit protected def webSocketToSocket(webSocket: WebSocket[_]): Socket =
    webSocket.socket

  def onMessageReceived(socket: WebSocket[A], message: Array[Byte])

  def onSocketConnected(socket: WebSocket[A])

  final def broadcastMessage(message: Array[Byte], sockets: WebSocket[A]*) = {
    sockets.foreach { socket =>
      socket.getOutputStream.write(socket.wsHandler.write(message))
      socket.getOutputStream.flush()
    }
  }

  override def ioLoop(socket: Socket) = {
    val keyAndPayloadOpt = new Handshaker(socket).shake[A]()

    if (keyAndPayloadOpt._1.isDefined &&
        keyAndPayloadOpt._2.isDefined) {
      val webSocket = new WebSocket[A](
        socket,
        keyAndPayloadOpt._1.get,
        keyAndPayloadOpt._2,
        onMessageReceived
      )

      Future {
        webSocket.wsHandler.listen()
      }

      onSocketConnected(webSocket)
    } else {
      socket.close()
    }
  }
}
