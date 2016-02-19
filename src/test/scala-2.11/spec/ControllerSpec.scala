package spec

import cortex.controller.ContentType._
import cortex.controller.Controller
import cortex.controller.HttpVerb._

import cortex.model.Primitive._
import cortex.util.log
import scala.util.Try
import scalaj.http.Http

import cortex.util.util._

import scala.language.implicitConversions

/**
  */
class ControllerSpec extends BaseSpec {

  override def controllers = Seq(
    new Controller {
      register("/hello", { req =>
        Option("Hello world")
      }, TextHtml, GET)

      register("/sum", { resp =>
        println("pre one")
        val one: String = resp.params.one
        println(one)
        val two: String = resp.params.two

        assert(one forall Character.isDigit)
        assert(two forall Character.isDigit)

        Option((one.toInt + two.toInt).toString)
      }, TextHtml, GET)

      register(
        "/updateEmail",
        resp => {
          if (resp.failed) {
            log d resp.missingParams.mkString(", ")
          }

          assert {
            val ok = Try(resp.params.id)
            if (ok.isFailure) {
              println(ok.failed.get.getMessage)
            }
            ok.isFailure
          }
          println("OK")
          assert(isValidEmail(resp.params.email))

          Option("success")
        },
        AllType,
        POST(
          "id" -> ulong,
          "email" -> string
        )
      )

      register(w"/user/${'id}/email", { resp =>
        resp.verb match {
          case GET =>
            val id: String = resp.params.id
            assert(id forall Character.isDigit)

            Option("success")
          case PUT =>
            val id: String = resp.params.id

            assert(id forall Character.isDigit)
            assert(isValidEmail(
              resp.params.email
            ))
            Option("success")
          case _ => None
        }
      }, ApplicationJson, GET, PUT)
    }
  )

  "An endpoint /hello" should "respond to a GET with 'Hello world'" in {
    async {
      Http(
        s"http://$localhost:$port/hello"
      ).asString.body
    } should equal("Hello world")
  }

  "An endpoint" should "GET the sum of the following numbers" in {
    async {
      Http(
        s"http://$localhost:$port/sum"
      ).params(Seq("one" -> "1", "two" -> "2")).asString.body
    } should equal("3")
  }

  "An endpoint" should "POST and consume our parameters" in {
    async {
      Http(
        s"http://$localhost:$port/updateEmail"
      ).postForm(Seq("id" -> "1", "email" -> "brady.thompson@gmail.com"))
        .asString.body
    } should equal("success")
  }

  "A REST endpoint" should "pass us data" in {
    async {
      Http(
        s"http://$localhost:$port/user/10/email"
      ).asString.body
    } should equal("success")
  }

  "A REST endpoint" should "consume our parameters" in {
    async {
      Http(
        s"http://$localhost:$port/user/10/email"
      ).postForm(Seq("email" -> "brady.thompson@gmail.com"))
        .method("PUT").asString.body
    } should equal("success")
  }
}
