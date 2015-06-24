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

  /**
   * Implicit conversion from string to byte array. This
   * acts as a convenience method so that a consumer of our
   * api can return a [[String]] without thinking twice.
   * @param string string to implicitly convert
   * @return string as byte array
   */
  implicit def toByteArray(string: String): Array[Byte] = string.getBytes

  /**
   * Register an endpoint with our server.
   * @param endpoint endpoint as a string (e.g., /tickets)
   * @param handler handler for responding to request
   * @param methods accepted http methods (GET, POST, etc.)
   */
  final def register(endpoint: String,
                     handler: (Response) => Option[Array[Byte]],
                     methods: HttpMethod*): Unit = {
    Controller.actionRegistrants +=
      s"/$endpoint" -> Action(handler, methods)
  }
}