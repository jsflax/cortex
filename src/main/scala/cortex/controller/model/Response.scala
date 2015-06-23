package cortex.controller.model

import java.net.URLDecoder
import cortex.controller.Controller.ContentType
import cortex.controller.Controller.HttpMethod._
import spray.json.JsonParser
import scala.language.implicitConversions

/**
 */
object Response {
  private def urlDecode(raw: String): String = URLDecoder.decode(raw, "UTF-8")

  implicit def parseQueryString(queryString: String): Map[String, String] = {
   (for {
      nameVal <- queryString.split("&").toList.map(_.trim).filter(_.length > 0)
      (name, value) <- nameVal.split("=").toList match {
        case Nil => None
        case n :: v :: _ => Some((urlDecode(n), urlDecode(v)))
        case n :: _ => Some((urlDecode(n), ""))
      }} yield (name, value)
     ).toMap
  }
}

import Response._

final case class Response(private val queryParams: String,
                          httpMethod: HttpMethod,
                          entity: Seq[Byte],
                          contentType: ContentType.Value) {
  val params: Map[String, String] =
    if (queryParams != null) queryParams else null
  lazy val json =
    if (contentType == ContentType.ApplicationJson) {
      JsonParser(new String(entity.toArray))
    } else {
      null
    }
}