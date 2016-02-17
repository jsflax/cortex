package cortex.io.ws

import java.net._

import cortex.controller.Controller
import cortex.io.IOManager

import scala.concurrent.ExecutionContext._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by jasonflax on 2/12/16.
  */
abstract class WsProtocolManager(port: Int,
                                 executionContext: ExecutionContext = global)
  extends IOManager(port, executionContext) with Controller {

  def onMessageReceived(socket: Socket, message: Array[Byte])
  def onSocketConnected(socket: Socket, key: String)

  final def broadcastMessage(message: Array[Byte], sockets: Socket*) = {
    sockets.foreach { socket =>
      socket.getOutputStream.write(WsWriter.write(message))
      socket.getOutputStream.flush()
    }
  }

  override def ioLoop(socket: Socket) = {
    val keyOpt = new Handshaker(socket).shake()

    Future {
      new InputListener(socket)(onMessageReceived)
    }

    if (keyOpt.isDefined) {
      onSocketConnected(socket, keyOpt.get)
    } else {
      socket.close()
    }
  }
}
