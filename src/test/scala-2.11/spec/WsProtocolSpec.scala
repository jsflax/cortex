package spec

import cortex.controller.HttpVerb.GET
import cortex.controller.{WsController, ContentType}
import cortex.io.ws.{WebSocket, WsProtocolManager}

/**
  * Created by jasonflax on 2/17/16.
  */
class WsProtocolSpec extends BaseSpec {
  override def ioManagers = Seq(
    new WsProtocolManager[String](8083) {
      override def onSocketConnected(socket: WebSocket[String]): Unit = {
        println(socket.token)
      }

      override def onMessageReceived(socket: WebSocket[String],
                                     message: Array[Byte]): Unit = {
        println(new String(message))
      }
    }
  )

  controllers ++= Seq(
    new WsController {
      register("connect", req => {
        "blah blah!"
      }, ContentType.AllType, GET)
    }
  )

  "A socket" should "actually read the message" in {
    synchronized {
      wait()
    }
  }
}
