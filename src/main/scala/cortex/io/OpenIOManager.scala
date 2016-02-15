package cortex.io

import java.io.{DataOutputStream, InputStreamReader, BufferedReader}
import java.net._

import cortex.controller.Controller

/**
  * Created by jasonflax on 2/12/16.
  */
abstract class OpenIOManager(port: Int) extends IOManager(port) with Controller {

  def onSocketConnected(socket: Socket, input: Input)

  override def ioLoop(socket: Socket) = {
    // retrieve input stream from socket
    val inputStream = new BufferedReader(
      new InputStreamReader(socket.getInputStream)
    )

    // read data from the input stream
    val input = readInput(inputStream)

    // if there were no errors reading the data, write output
    if (input.isDefined) {
      writeOutput(
        input.get.message,
        input.get.contentType,
        new DataOutputStream(socket.getOutputStream),
        closed = false
      )

      onSocketConnected(socket, input.get)
    } else {
      socket.close()
    }
  }
}
