package cortex.io

import java.io.{DataOutputStream, InputStreamReader, BufferedReader}
import java.net.Socket

import cortex.util.log

import scala.concurrent.ExecutionContext

/**
  * Created by jasonflax on 2/13/16.
  */
class ClosedIOManager(port: Int,
                      executionContext: ExecutionContext = ExecutionContext.global)
  extends IOManager(port: Int, executionContext) {

  override def ioLoop(socket: Socket) = {
    log trace "Reading Input"
    // retrieve input stream from socket
    val inputStream = new BufferedReader(
      new InputStreamReader(socket.getInputStream)
    )

    log trace "Done reading input"

    // read data from the input stream
    val input = readInput(inputStream)

    // if there were no errors reading the data, write output
    if (input.isDefined) {
      writeOutput(
        input.get.message,
        input.get.contentType,
        new DataOutputStream(socket.getOutputStream))
    }

    log trace "Done writing output"

    // close the socket
    socket.close()
  }
}
