package cortex.view

import cortex.controller.Controller
import cortex.controller.{HttpMethod, ContentType}

import scala.io.BufferedSource

/**
 */
trait Images extends View with Controller {
  def favicon: BufferedSource

  override protected[view] def encoding = scala.io.Codec.ISO8859

  register("favicon.ico", { resp =>
    val favOpt = Option(favicon.map(_.toByte).toArray)
    favicon.close()
    favOpt
  }, ContentType.ImageIco, HttpMethod.GET)

  for ((fileName, extAndGenFunc) <- fields) {
    register(s"/images/$fileName${extAndGenFunc._1}", { resp =>
      Option(extAndGenFunc._2(null))
    }, extAndGenFunc._1 match {
      case ".png" => ContentType.ImagePng
      case ".webp" => ContentType.ImageWebp
      case _ => ContentType.ImagePng
    }, HttpMethod.GET)
  }
}
