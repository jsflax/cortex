package cortex.io

import cortex.controller.ContentType._
import cortex.controller.{HttpMessage, Message, Action, HttpVerb}
import cortex.model.{Request, Primitive}
import cortex.util.log
import spray.json.{JsString, JsBoolean, JsObject}

import scala.util.{Failure, Success, Try}

/**
  * Created by jasonflax on 2/19/16.
  */
/**
  * Dumb datum for passing around input information.
  *
  * @param endpoint    endpoint being targeting
  * @param body        input body for non-GET calls
  * @param queryParams query parameters in URL or in post body
  * @param httpMethod  http method being used (GET, POST, etc.)
  * @param action      action associated with this endpoint
  * @param contentType accepted content-types
  */
private[io] case class Input(endpoint: String,
                             body: IndexedSeq[Byte],
                             queryParams: String,
                             cookie: Option[String],
                             httpMethod: HttpVerb[_ <: Primitive[_]],
                             headers: Map[String, String],
                             action: Action,
                             contentType: ContentType) {
  // call the handler on the registered action to parse the input
  // and fetch the output (response)
  lazy val message: Message[_] =
    Try(
      action.handler(
        Request(
          queryParams = queryParams,
          verb = httpMethod,
          headers = headers,
          entity = body,
          extractedParams = action.actionContext.map(endpoint),
          contentType = contentType,
          cookie = cookie
        )
      )
    ) match {
      case Success(msg) => msg
      case Failure(msg) =>
        log e s"Internal server error: ${msg.getMessage}"
        HttpMessage(
          Option(
            JsObject(
              "success" -> JsBoolean(false),
              "error" -> JsString("Internal server error")
            ).toString().getBytes
          ),
          cookie,
          None
        )
    }
}
