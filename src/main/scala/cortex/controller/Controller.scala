package cortex.controller

import cortex.controller.Controller.Action
import cortex.controller.model.Response
import language.postfixOps
import scala.collection.mutable
import scala.language.implicitConversions

/**
 * Master controller object. Maintains a map of all
 * of the registered controllers, which contain
 * all of the registered endpoints.
 */
object Controller {

  /**
   * List of http 1.1 method specs.
   */
  implicit object HttpMethod extends Enumeration {
    type HttpMethod = Value
    val GET, POST, PATCH, DELETE, PUT = Value
  }

  /**
   * List of http 1.1 content type specs.
   *///TODO: incomplete list
  object ContentType extends Enumeration {
    type ContentType = Value

    // map all values by their string representation to
    // make them easily retrievable
    lazy val valueMap = values map (v => v.toString -> v) toMap

    val NoneType = Value
    val AllType = Value("*/*")
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