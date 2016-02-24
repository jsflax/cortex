package cortex.io.ws

import java.net.Socket

import cortex.controller.WsMessage

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
                       payload: Option[WsMessage[A]],
                       messageReceivedListener: (WebSocket[A], Array[Byte]) => Unit) = {
    this()

    this.socket = socket
    this._token = token
    payload match {
      case Some(load) => this._payload = Option(load.response)
      case _ =>
    }
    this.wsHandler = new WsHandler[A](this)(messageReceivedListener)
  }
}
