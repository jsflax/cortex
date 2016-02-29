package spec

import cortex.controller.ContentType._
import cortex.controller.HttpController
import cortex.controller.HttpVerb.POST
import spray.json._

import scalaj.http.Http

/**
  * Created by jasonflax on 2/23/16.
  */
class DynamicsSpec extends BaseSpec {
  controllers +=
    new HttpController {
      register(
        "/jsonFlat",
        req => {
          val number: Int = ~req.params.number
          val string: String = ~req.params.string
          val bool: Boolean = ~req.params.bool

          assert(number == 1)
          assert(string.equals("hello"))
          assert(bool)

          wrap(success = true)
        },
        ApplicationJson,
        POST
      )

      register(
        "/jsonNested",
        req => {
          val hiddenNumber: Int =
            ~req.params.one.two.three.hiddenNumber

          assert(hiddenNumber == 42)

          wrap(success = true)
        },
        ApplicationJson,
        POST
      )
    }

  "A flat json based controller" should "respond to a flat json object" in {
    JsonParser(
      Http(s"http://$localhost:$port/jsonFlat")
        .header("content-type", "application/json")
        .postData(
          JsObject(
            "number" -> JsNumber(1),
            "string" -> JsString("hello"),
            "bool" -> JsBoolean(true)
          ).toString()
        ).asString.body
    ).asJsObject.fields(
      "success"
    ).asInstanceOf[JsBoolean].value should equal(true)
  }

  "A nested json based controller" should "respond to a nested json object" in {
    JsonParser(
      Http(s"http://$localhost:$port/jsonNested")
        .header("content-type", "application/json")
        .postData(
          JsObject(
            "one" -> JsObject(
              "two" -> JsObject(
                "three" -> JsObject(
                  "hiddenNumber" -> JsNumber(42)
                )
              )
            )
          ).toString()
        ).asString.body
    ).asJsObject.fields(
      "success"
    ).asInstanceOf[JsBoolean].value should equal(true)
  }
}
