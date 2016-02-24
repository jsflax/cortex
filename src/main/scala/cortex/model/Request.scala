package cortex.model

import java.net.URLDecoder
import cortex.controller._
import cortex.util.DynamicMap
import spray.json._
import scala.language.implicitConversions

/**
  * Response helper object.
  */
object Request {
  /**
    * Convenience method to decode url params to a normal string
    *
    * @param raw raw, encoded url params
    * @return decoded url params
    */
  @inline private def urlDecode(raw: String): String =
    URLDecoder.decode(raw, "UTF-8")

  /**
    * Implicitly convert a query string to a map.
    *
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
  *
  * @param queryParams     string form query params that will be coerced to a map
  * @param verb            http method being called
  * @param entity          request body if applicable
  * @param contentType     requested content type
  * @param extractedParams params extracted from wildcard url
  */
final case class Request(queryParams: String,
                         verb: HttpVerb[_ <: Primitive[_]],
                         headers: Map[String, String],
                         entity: Seq[Byte],
                         contentType: ContentType.Value,
                         extractedParams: Map[String, String] = Map.empty,
                         cookie: Option[String]) {

  var missingParams: Seq[String] = Seq()
  var failed = false

  /** coerced query parameters if applicable */
  lazy val params: DynamicMap = {

    val params =
      extractedParams ++ (
        if (queryParams != null) {
          parseQueryString(queryParams)
        } else {
          Map.empty[String, String]
        }) ++ (
        contentType match {
          case ContentType.ApplicationFormUrlEncoded =>
            parseQueryString(new String(entity.toArray))
          case ContentType.ApplicationJson =>
            def mapJson: JsValue => Any = {
              case JsString(s) => s
              case JsNumber(n) => n match {
                case int if n.isValidInt => int.intValue()
                case double if n.isDecimalDouble => double.doubleValue()
                case float if n.isDecimalFloat => float.floatValue()
                case long if n.isValidLong => long.longValue()
                case short if n.isValidShort => short.shortValue()
              }
              case JsBoolean(b) => b
              case JsObject(o) => o.mapValues(mapJson)
              case JsArray(a) => a.map(mapJson)
            }

            JsonParser.apply(
              new String(entity.toArray)
            ).asJsObject.fields.mapValues(mapJson)
          case _ => Map.empty[String, String]
        })

    if (verb.params.nonEmpty) {
      if (params.keys.forall(verb.params.keySet.contains)) {
        val coercedMap = params.map {
          case (key: String, value: String) =>
            val prim: Primitive[_] = verb.params(key)

            val coerced = prim.coerce(value)

            if (coerced.isFailure) {
              failed = true
              key -> null
            } else {
              key -> coerced.get
            }
        }

        new DynamicMap(
          coercedMap
        )
      } else {
        failed = true
        missingParams = verb.params.keySet.diff(params.keySet).toSeq
        new DynamicMap(params)
      }
    } else {
      new DynamicMap(params)
    }
  }
}