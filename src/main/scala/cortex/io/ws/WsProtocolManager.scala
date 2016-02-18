package cortex.io.ws

import java.net._

import cortex.controller.Controller
import cortex.io.IOManager

import scala.concurrent.ExecutionContext._
import scala.concurrent.{ExecutionContext, Future}

import scala.language.implicitConversions

/**
  * Created by jasonflax on 2/12/16.
  */
abstract class WsProtocolManager(port: Int,
                                 executionContext: ExecutionContext = global)
  extends IOManager(port, executionContext) with Controller {

  implicit protected def webSocketToSocket(webSocket: WebSocket): Socket =
    webSocket.socket

  def onMessageReceived(socket: WebSocket, message: Array[Byte])

  def onSocketConnected(socket: WebSocket)

  final def broadcastMessage(message: Array[Byte], sockets: WebSocket*) = {
    sockets.foreach { socket =>
      socket.getOutputStream.write(socket.wsHandler.write(message))
      socket.getOutputStream.flush()
    }
  }

  override def ioLoop(socket: Socket) = {
    val keyOpt = new Handshaker(socket).shake()

    if (keyOpt.isDefined) {
      val webSocket = new WebSocket(
        socket,
        keyOpt.get,
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
