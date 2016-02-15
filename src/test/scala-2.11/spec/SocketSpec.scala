package spec

import java.io.DataOutputStream
import java.net.Socket
import java.util.UUID

import cortex.controller.Controller.Message
import cortex.controller.{ContentType, HttpMethod}
import cortex.io.OpenIOManager
import spray.json.{JsString, JsObject}

/**
  * Created by jasonflax on 2/13/16.
  */
class SocketSpec extends BaseSpec {

  object OpenIOController extends OpenIOManager(8082) {
    register("connect/token", req => {
      JsObject(
        "token" -> JsString(UUID.randomUUID().toString)
      )
    }, ContentType.ApplicationJson, HttpMethod.GET)

    override def onSocketConnected(socket: Socket, input: Input) = {
      println(socket.toString)

      val outputStream = new DataOutputStream(socket.getOutputStream)
      outputStream.flush()

      writeOutput(
        Message(Option(JsObject("message" -> JsString("hey")).toString())),
        ContentType.ApplicationJson,
        outputStream,
        closed = false
      )
    }
  }

  override val ioManagers = Seq(OpenIOController)
}
