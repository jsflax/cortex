package cortex.controller

import scala.language.implicitConversions

/**
  * Created by jasonflax on 2/19/16.
  */
trait WsController extends Controller[WsMessage[_]] {
  implicit def anyToMessage(any: Any): WsMessage[_] =
    WsMessage(any)
}
