package cortex.model

import java.net.URLDecoder
import cortex.controller.ContentType
import cortex.controller.HttpMethod._
import spray.json.JsonParser
import scala.language.implicitConversions

/**
 * Response helper object.
 */
object Request {
  /**
   * Convenience method to decode url params to a normal string
   * @param raw raw, encoded url params
   * @return decoded url params
   */
  @inline private def urlDecode(raw: String): String =
    URLDecoder.decode(raw, "UTF-8")

  /**
   * Implicitly convert a query string to a map.
   * @param queryString query string to convert
   * @return
   */
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

import Request._

/**
 * Datum for response information.
 * @param queryParams string form query params that will be coerced to a map
 * @param httpMethod http method being called
 * @param entity request body if applicable
 * @param contentType requested content type
 * @param extractedParams params extracted from wildcard url
 * @param cookie cookies passed up with request
 */
final case class Request(queryParams: String,
                         httpMethod: HttpMethod,
                         entity: Seq[Byte],
                         contentType: ContentType.Value,
                         extractedParams: Map[String, String] = Map.empty,
                         cookie: Option[String] = None) {

  /** coerced query parameters if applicable */
  val params: Map[String, String] =
    extractedParams ++ (if (queryParams != null) {
      parseQueryString(queryParams)
    } else if (contentType == ContentType.ApplicationFormUrlEncoded) {
      parseQueryString(new String(entity.toArray))
    } else {
      Map.empty[String, String]
    })

  /** posted json parameters if applicable */
  lazy val json =
    if (contentType == ContentType.ApplicationJson) {
      JsonParser(new String(entity.toArray))
    } else {
      null
    }
}