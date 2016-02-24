package cortex.view

import cortex.controller.{HttpController, HttpVerb, Controller, ContentType}

import scala.io.BufferedSource

/**
 */
trait Images extends View with HttpController {
  def favicon: Option[Array[Byte]]

  override protected[view] def encoding = scala.io.Codec.ISO8859

  register("favicon.ico", { resp =>
    favicon
  }, ContentType.ImagePng, HttpVerb.GET)

  for ((fileName, extAndGenFunc) <- fields) {
    register(s"/images/$fileName${extAndGenFunc._1}", { resp =>
      Option(extAndGenFunc._2(null))
    }, extAndGenFunc._1 match {
      case ".png" => ContentType.ImagePng
      case ".webp" => ContentType.ImageWebp
      case _ => ContentType.ImagePng
    }, HttpVerb.GET)
  }
}
