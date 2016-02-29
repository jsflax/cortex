package cortex.io.ws

import java.net.Socket

import cortex.controller.WsMessage
import cortex.util.log

/**
  * Created by jasonflax on 2/17/16.
  */
class WebSocket[A] {
  private[ws] var socket: Socket = null
  private[ws] var wsHandler: WsHandler[_] = null

  private var _token: String = null
  lazy val token = _token

  private var _payload: Option[A] = None

  lazy val payload: A = _payload.get

  private[ws] def this(socket: Socket,
                       token: String,
                       message: Option[WsMessage[A]],
                       messageReceivedListener: (WebSocket[A], Array[Byte]) => Unit) = {
    this()

    this.socket = socket
    this._token = token

    message match {
      case Some(msg) => this._payload = Some(msg.response)
      case _ =>
    }

    this.wsHandler = new WsHandler[A](this)(messageReceivedListener)
  }
}
