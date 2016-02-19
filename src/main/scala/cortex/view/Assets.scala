package cortex.view

import cortex.controller.{HttpVerb, Controller, ContentType}

/**
 */
trait Assets extends View with Controller {

  for ((fileName, extAndGenFunc) <- fields) {
    register(s"/assets/$fileName${extAndGenFunc._1}", { resp =>
      Option(extAndGenFunc._2(null))
    }, extAndGenFunc._1 match {
      case ".css" => ContentType.TextCss
      case ".js" => ContentType.TextJavascript
      case ".ttf" => ContentType.FontTrueType
      case ".otf" => ContentType.FontOpenType
      case _ => ContentType.TextHtml
    }, HttpVerb.GET)
  }
}
