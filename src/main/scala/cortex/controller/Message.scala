package cortex.controller


/**
  * Created by jasonflax on 2/19/16.
  */
trait Message[A] {
  val response: A
}

case class HttpMessage(response: Option[Array[Byte]],
                       cookie: Option[String] = None,
                       redirect: Option[String] = None)
  extends Message[Option[Array[Byte]]]

case class WsMessage[A](response: A) extends Message[A]
