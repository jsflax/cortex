package spec

import cortex.controller.{ContentType, Controller}
import cortex.io.ws.{WebSocket, WsProtocolManager}

/**
  * Created by jasonflax on 2/17/16.
  */
class WsProtocolSpec extends BaseSpec {
  override def ioManagers = Seq(
    new WsProtocolManager(8083) {
      override def onSocketConnected(socket: WebSocket): Unit = {
        println(socket.token)
      }

      override def onMessageReceived(socket: WebSocket,
                                     message: Array[Byte]): Unit = {
        println(new String(message))
      }
    }
  )

  override def controllers = Seq(
    new Controller {
      register("connect", req => { None }, ContentType.AllType)
    }
  )

  "" should "" in {
    start()

    synchronized {
      wait()
    }
  }
}
