package cortex.io.ws

import java.net.Socket

/**
  * Created by jasonflax on 2/17/16.
  */
class WebSocket {
  private[ws] var socket: Socket = null
  private[ws] var wsHandler: WsHandler = null

  private var _token: String = null
  lazy val token = _token

  private[ws] def this(socket: Socket,
                       token: String,
                       messageReceivedListener: (WebSocket, Array[Byte]) => Unit) = {
    this()

    this.socket = socket
    this._token = token
    this.wsHandler = new WsHandler(this)(messageReceivedListener)
  }
}
