package cortex.io.ws

import java.net.Socket

/**
  * Created by jasonflax on 2/16/16.
  */
private[ws] class InputListener(socket: Socket)
                               (implicit messageReceivedListener:
                                (Socket, Array[Byte]) => Unit) {
  while (socket.isConnected) {
    messageReceivedListener(
      socket,
      WsReader.read(Stream.continually(socket.getInputStream.read))
    )
  }
}
