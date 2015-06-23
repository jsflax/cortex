package cortex.controller

import cortex.controller.Controller.Action
import cortex.controller.model.Response

import language.postfixOps
import scala.collection.mutable
import scala.language.implicitConversions

/**
 */
object Controller {
  implicit object HttpMethod extends Enumeration {
    type HttpMethod = Value
    val GET, POST, PATCH, DELETE, PUT = Value
  }

  object ContentType extends Enumeration {
    type ContentType = Value

    val NoneType = Value

    lazy val valueMap = values map (v => v.toString -> v) toMap

    val ApplicationOctetStream = Value("application/octet-stream")
    val ApplicationJson = Value("application/json")
    val ApplicationFormUrlEncoded = Value("application/x-www-form-urlencoded")

    val TextHtml = Value("text/html")
  }


  import cortex.controller.Controller.HttpMethod._

  case class Action[Response](handler: (Response) => Option[Array[Byte]],
                                  methods: Seq[HttpMethod])

  lazy val actionRegistrants = new mutable.HashMap[String, Action[Response]]()
}

trait Controller {
  import cortex.controller.Controller.HttpMethod._

  implicit def toByteArray(string: String): Array[Byte] = string.getBytes

  final def register(endpoint: String,
                     handler: (Response) => Option[Array[Byte]],
                     methods: HttpMethod*): Unit = {
    Controller.actionRegistrants +=
      s"/$endpoint" -> Action(handler, methods)
  }
}