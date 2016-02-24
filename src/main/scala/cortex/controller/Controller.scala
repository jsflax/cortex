package cortex.controller

import cortex.model.{Primitive, ActionContext, Request}
import cortex.util.{DynamicValue, log}
import spray.json._
import language.postfixOps
import scala.collection.mutable
import scala.language.implicitConversions
import ContentType._
import scala.language.postfixOps

/**
  * Master controller object. Maintains a map of all
  * of the registered controllers, which contain
  * all of the registered endpoints.
  */
private[cortex] object Controller {
  lazy val actionRegistrants = new mutable.ListBuffer[Action]()
}


private[cortex] trait Controller[A <: Message[_]] {

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

  /**
    * Register an endpoint with our server.
    *
    * @param actionContext endpoint as a string (e.g., /tickets)
    * @param handler       handler for responding to request
    * @param verbs         accepted http methods (GET, POST, etc.)
    */
  def register(actionContext: ActionContext,
               handler: (Request) => A,
               contentType: ContentType,
               verbs: HttpVerb[_ <: Primitive[_]]*): Unit =
    Controller.actionRegistrants += Action(
      handler,
      contentType,
      actionContext,
      verbs
    )
}
