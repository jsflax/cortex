package cortex.io.ws

import java.net._

import cortex.controller.WsController
import cortex.io.IOManager
import cortex.util.log

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
      if (!socket.isClosed) {
        socket.getOutputStream.write(socket.wsHandler.write(message))
        socket.getOutputStream.flush()
      }
    }
  }

  override def ioLoop(socket: Socket) = {
    val (key, payload) = new Handshaker(socket).shake[A]()

    if (key.isDefined &&
      payload.isDefined) {

      val webSocket = new WebSocket(
        socket,
        key.get,
        payload,
        onMessageReceived
      )

      Future {
        try {
          webSocket.wsHandler.listen()
        } catch {
          case e: Exception => e.printStackTrace(System.err)
        }
      }

      onSocketConnected(webSocket)
    } else {
      log e s"key: $key payload: $payload (one was not defined)"
      socket.close()
    }
  }
}
