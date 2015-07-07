package cortex.controller

import cortex.controller.Controller.Action
import cortex.model.{ActionContext, Request}
import language.postfixOps
import scala.collection.mutable
import scala.language.implicitConversions

/**
 * List of http 1.1 method specs.
 */
object HttpMethod extends Enumeration {
  type HttpMethod = Value
  val GET, POST, PATCH, DELETE, PUT = Value
}

/**
 * List of http 1.1 content type specs.
 */
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
  val TextJavascript = Value("text/javascript")
  val TextCss = Value("text/css")

  val ImageWebp = Value("image/webp")
  val ImagePng = Value("image/png")
  val ImagePngBase64 = Value("image/png;base64")
  val ImageIco = Value("image/ico")
}

import HttpMethod._
import ContentType._

/**
 * Master controller object. Maintains a map of all
 * of the registered controllers, which contain
 * all of the registered endpoints.
 */
object Controller {

  case class Message(response: Option[Array[Byte]],
                     cookie: Option[String] = None,
                     redirect: Option[String] = None)

  case class Action(handler: (Request) => Message,
                    contentType: ContentType,
                    actionContext: ActionContext,
                    methods: Seq[HttpMethod])

  lazy val actionRegistrants = new mutable.ListBuffer[Action]()
}

trait Controller {
  import cortex.controller.Controller.Message


  /**
   * Implicit conversion from string to byte array. This
   * acts as a convenience method so that a consumer of our
   * api can return a [[String]] without thinking twice.
   * @param string string to implicitly convert
   * @return string as byte array
   */
  implicit def toByteArray(string: String): Array[Byte] = string.getBytes

  implicit def optToByteArrayOpt(optString: Option[String]): Option[Array[Byte]] =
    optString match {
      case Some(str) => Option(str)
      case None => Option.empty[Array[Byte]]
    }

  implicit def optStrToMessage(opt: Option[String]): Message =
    Message(opt)

  implicit def optToMessage(opt: Option[Array[Byte]]): Message =
    Message(opt)

  implicit def noneToMessage(opt: Option[Nothing]): Message = Message(None)

  implicit def stringToActionContext(string: String): ActionContext =
    ActionContext(string)(Seq())

  /**
   * Register an endpoint with our server.
   * @param actionContext endpoint as a string (e.g., /tickets)
   * @param handler handler for responding to request
   * @param methods accepted http methods (GET, POST, etc.)
   */
  final def register(actionContext: ActionContext,
                     handler: (Request) => Message,
                     contentType: ContentType,
                     methods: HttpMethod*): Unit =
    Controller.actionRegistrants += Action(
      handler,
      contentType,
      actionContext,
      methods
    )
}
