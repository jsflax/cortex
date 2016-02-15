package cortex.controller

import cortex.controller.Controller.Action
import cortex.model.{ActionContext, Request}
import spray.json._
import language.postfixOps
import scala.collection.mutable
import scala.language.implicitConversions

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


  implicit val strFormat: JsonFormat[String] =
    new JsonFormat[String] {
      override def read(value: JsValue) = value.asInstanceOf[JsString].value

      override def write(str: String) = JsString(str)
    }

  implicit val intFormat: JsonFormat[Int] =
    new JsonFormat[Int] {
      override def read(value: JsValue) = value.asInstanceOf[JsNumber].value.toInt

      override def write(str: Int) = JsNumber(str)
    }

  implicit val longFormat: JsonFormat[Long] =
    new JsonFormat[Long] {
      override def read(value: JsValue) = value.asInstanceOf[JsNumber].value.toLong

      override def write(str: Long) = JsNumber(str)
    }

  implicit val boolFormat: JsonFormat[Boolean] =
    new JsonFormat[Boolean] {
      override def read(value: JsValue) = value.asInstanceOf[JsBoolean].value

      override def write(str: Boolean) = JsBoolean(str)
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

  implicit def strTupToJs(strTup: (String, String)): (String, JsValue) =
    strTup._1 -> JsString(strTup._2)

  implicit def jsToMessage(jsValue: JsValue): Message =
    optJsToMessage(Option(jsValue))

  implicit def optJsToMessage(opt: Option[JsValue]): Message = opt match {
    case Some(o) => Message(Option(o.toString().getBytes))
    case None => Message(None)
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
    *
    * @param actionContext endpoint as a string (e.g., /tickets)
    * @param handler       handler for responding to request
    * @param methods       accepted http methods (GET, POST, etc.)
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
