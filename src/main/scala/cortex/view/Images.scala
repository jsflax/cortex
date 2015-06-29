package cortex.view

import cortex.controller.Controller
import cortex.controller.{HttpMethod, ContentType}

/**
 */
trait Images extends View with Controller {
  override protected[view] def encoding = scala.io.Codec.ISO8859


  for ((fileName, extAndGenFunc) <- fields) {
    register(s"/images/$fileName${extAndGenFunc._1}", { resp =>
      Option(extAndGenFunc._2(null))
    }, extAndGenFunc._1 match {
      case ".png" => ContentType.ImagePng
      case _ => ContentType.ImagePng
    }, HttpMethod.GET)
  }
}
