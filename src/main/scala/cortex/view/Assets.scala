package cortex.view

import cortex.controller.{HttpController, HttpVerb, ContentType}

/**
 */
trait Assets extends View with HttpController {

  for (file <- listFiles(viewDir)) {
    register(s"/assets/${file.getName}", { resp =>
      val source = scala.io.Source.fromFile(file)(encoding)
      val byteArray = source.map(_.toByte).toArray
      source.close()
      Option(byteArray)
    }, {
      val fileSplit = file.getName.split("\\.")
      fileSplit.length match {
        case 1 => ContentType.TextHtml
        case _ => fileSplit.slice(1, fileSplit.length).mkString(".") match {
          case "css" => ContentType.TextCss
          case "js" | "min.js" => ContentType.TextJavascript
          case "ttf" => ContentType.FontTrueType
          case "otf" => ContentType.FontOpenType
          case "css.map" | "js.map" | "min.js.map" | "min.css.map" =>
            ContentType.ApplicationJson
          case _ => ContentType.TextHtml
        }
      }
    }, HttpVerb.GET)
  }
}
