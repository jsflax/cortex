package spec

import cortex.controller.{WsController, ContentType}
import cortex.io.ws.{WebSocket, WsProtocolManager}

/**
  * Created by jasonflax on 2/17/16.
  */
class WsProtocolSpec extends BaseSpec {
  override def ioManagers = Seq(
    new WsProtocolManager[Any](8083) {
      override def onSocketConnected(socket: WebSocket[Any]): Unit = {
        println(socket.token)
      }

      override def onMessageReceived(socket: WebSocket[Any],
                                     message: Array[Byte]): Unit = {
        println(new String(message))
      }
    }
  )

 controllers ++= Seq(
    new WsController {
      register("connect", req => { None }, ContentType.AllType)
    }
  )
}
