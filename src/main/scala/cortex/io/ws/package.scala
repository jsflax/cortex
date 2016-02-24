package cortex.io

import java.net.Socket

import scala.language.implicitConversions

/**
  * Created by jasonflax on 2/16/16.
  */
package object ws {
  implicit private [ws] def intToByte(int: Int): Byte = int.toByte
  implicit def webSocketToSocket(webSocket: WebSocket[_]): Socket =
    webSocket.socket
}
