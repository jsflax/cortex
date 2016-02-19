package cortex.controller

import cortex.controller.Controller.Action
import cortex.model.{Primitive, ActionContext, Request}
import spray.json._
import language.postfixOps
import scala.collection.mutable
import scala.language.implicitConversions
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
                    methods: Seq[HttpVerb[_ <: Primitive[_]]])

  lazy val actionRegistrants = new mutable.ListBuffer[Action]()
}

trait Controller {

  import cortex.controller.Controller.Message

  implicit class WildcardContext(sc: StringContext) {
    def w(implicit args: Symbol*): ActionContext = {
      ActionContext(sc.s(args.map(_ => "(.+)"): _*))(args)
    }
  }

  /**
    * Implicit conversion from string to byte array. This
    * acts as a convenience method so that a consumer of our
    * api can return a [[String]] without thinking twice.
    *
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

  implicit def strToMessage(str: String): Message =
    Message(Option(str))

  implicit def stringToActionContext(string: String): ActionContext =
    ActionContext(string)(Seq())


  implicit def stringToJsString(string: String): JsString = JsString(string)

  implicit def booleanToJsBoolean(boolean: Boolean): JsBoolean =
    JsBoolean(boolean)

  implicit def doubleToJsNumber(double: Double): JsNumber = JsNumber(double)

  implicit def longToJsNumber(long: Long): JsNumber = JsNumber(long)

  implicit def intToJsNumber(int: Int): JsNumber = JsNumber(int)

  implicit def mapToJsObject(map: Map[String, JsValue]): JsObject =
    JsObject(map)

  implicit def seqToJsArray(seq: Vector[JsValue]): JsArray = JsArray(seq)

  implicit def stringStringToStringJsValue(pair: (String, String)): (String, JsValue) =
    pair._1 -> pair._2

  implicit def stringBooleanToStringJsValue(pair: (String, Boolean)): (String, JsValue) =
    pair._1 -> pair._2

  implicit def stringDoubleToStringJsValue(pair: (String, Double)): (String, JsValue) =
    pair._1 -> pair._2

  implicit def stringLongToStringJsValue(pair: (String, Long)): (String, JsValue) =
    pair._1 -> pair._2

  implicit def stringIntToStringJsValue(pair: (String, Int)): (String, JsValue) =
    pair._1 -> pair._2

  implicit def stringMapToStringJsValue(pair: (String, Map[String, JsValue])): (String, JsValue) =
    pair._1 -> pair._2

  implicit def stringVecToStringJsValue(pair: (String, Vector[JsValue])): (String, JsValue) =
    pair._1 -> pair._2

  def obj(values: (String, JsValue)*) = JsObject(values: _*)

  def wrap(success: Boolean, values: (String, JsValue)*) = {
    Message(
      Option(
        JsObject(
          "success" -> JsBoolean(success),
          "data" -> JsObject(
            values: _*
          )
        ).toString()
      )
    )
  }

  def wrap(success: Boolean, values: JsArray) = {
    Message(
      Option(
        JsObject(
          "success" -> JsBoolean(success),
          "data" -> values
        ).toString()
      )
    )
  }

  /**
    * Register an endpoint with our server.
    *
    * @param actionContext endpoint as a string (e.g., /tickets)
    * @param handler       handler for responding to request
    * @param verbs         accepted http methods (GET, POST, etc.)
    */
  final def register(actionContext: ActionContext,
                     handler: (Request) => Message,
                     contentType: ContentType,
                     verbs: HttpVerb[_ <: Primitive[_]]*): Unit =
    Controller.actionRegistrants += Action(
      handler,
      contentType,
      actionContext,
      verbs
    )
}
