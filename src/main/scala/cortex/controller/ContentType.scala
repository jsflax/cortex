package cortex.controller

import scala.language.postfixOps

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

  val MultipartFormData = Value("multipart/form-data")

  val TextHtml = Value("text/html")
  val TextJavascript = Value("text/javascript")
  val TextCss = Value("text/css")

  val ImageWebp = Value("image/webp")
  val ImagePng = Value("image/png")
  val ImagePngBase64 = Value("image/png;base64")
  val ImageIco = Value("image/ico")

  val FontOpenType = Value("application/x-font-opentype")
  val FontTrueType = Value("application/x-font-truetype")
}
