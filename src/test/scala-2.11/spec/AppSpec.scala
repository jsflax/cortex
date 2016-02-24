package spec

import cortex.controller.{HttpController, HttpVerb, ContentType, Controller}
import spray.json.{JsString, JsObject}

import scalaj.http.Http

/**
  * Created by jasonflax on 2/14/16.
  */
class AppSpec extends BaseSpec {

  controllers +=
    new HttpController {
      register("helloJson", req => {
        JsObject("message" -> JsString("hello")).toString()
      }, ContentType.ApplicationJson, HttpVerb.GET)
    }

  "An endpoint /helloJson" should "respond with {\"message\": \"hello\"}" in {
    async {
      Http(
        s"http://$localhost:$port/helloJson"
      ).asString.body
    } should equal(JsObject("message" -> JsString("hello")).toString())
  }
}
