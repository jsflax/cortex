package cortex.controller

import spray.json.{JsArray, JsBoolean, JsObject, JsValue}

import scala.language.implicitConversions

/**
  * Created by jasonflax on 2/19/16.
  */
trait HttpController extends Controller[HttpMessage] {
  implicit def optStrToMessage(opt: Option[String]): HttpMessage =
    HttpMessage(opt)

  implicit def optToMessage(opt: Option[Array[Byte]]): HttpMessage =
    HttpMessage(opt)

  implicit def noneToMessage(opt: Option[Nothing]): HttpMessage = HttpMessage(None)

  implicit def strToMessage(str: String): HttpMessage =
    HttpMessage(Option(str))

  def wrap(success: Boolean, values: (String, JsValue)*) = {
    HttpMessage(
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
    HttpMessage(
      Option(
        JsObject(
          "success" -> JsBoolean(success),
          "data" -> values
        ).toString()
      )
    )
  }
}
