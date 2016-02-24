package cortex.view

import cortex.controller.{HttpController, HttpVerb, ContentType}
import cortex.util.log

/**
 */
trait Assets extends View with HttpController {

  for ((fileName, extAndGenFunc) <- fields) {
    register(s"/assets/$fileName${extAndGenFunc._1}", { resp =>
      Option(extAndGenFunc._2(null))
    }, extAndGenFunc._1 match {
      case ".css" => ContentType.TextCss
      case ".js" | ".min.js" => ContentType.TextJavascript
      case ".ttf" => ContentType.FontTrueType
      case ".otf" => ContentType.FontOpenType
      case _ => ContentType.TextHtml
    }, HttpVerb.GET)
  }
}
