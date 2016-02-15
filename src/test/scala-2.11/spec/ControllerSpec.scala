package spec

import cortex.controller.ContentType._
import cortex.controller.Controller
import cortex.controller.HttpMethod._

import scalaj.http.Http

/**
 */
class ControllerSpec extends BaseSpec {
  override def controllers = Seq(
    new Controller {
      register("/hello", { resp =>
        Option("Hello world")
      }, TextHtml, GET)

      register("/sum", { resp =>
        val one: String = resp.params("one")
        val two: String = resp.params("two")

        assert(one forall Character.isDigit)
        assert(two forall Character.isDigit)

        Option((one.toInt + two.toInt).toString)
      }, TextHtml, GET)

      import cortex.util.util._

      register("/updateEmail", { resp =>
        val id: String = resp.params("id")
        val email: String = resp.params("email")

        assert(id forall Character.isDigit)
        assert(isValidEmail(email))

        Option("success")
      }, AllType, POST)

      register(w"/user/${'id}/email", { resp =>
        resp.httpMethod match {
          case GET =>
            val id: String = resp.params("id")
            assert(id forall Character.isDigit)

            Option("success")
          case PUT =>
            val id: String = resp.params("id")

            assert(id forall Character.isDigit)
            assert(isValidEmail(
              resp.params("email")
            ))
            Option("success")
          case _ => None
        }
      }, ApplicationJson, GET, PUT)
    }
  )

  "An endpoint" should "respond to a GET with 'Hello world'" in {
    async {
      Http(
        s"http://$localhost:$port/hello"
      ).asString.body
    } should equal ("Hello world")
  }

  "An endpoint" should "GET the sum of the following numbers" in {
    async {
      Http(
        s"http://$localhost:$port/sum"
      ).params(Seq("one" -> "1", "two" -> "2")).asString.body
    } should equal ("3")
  }

  "An endpoint" should "POST and consume our parameters" in {
    async {
      Http(
        s"http://$localhost:$port/updateEmail"
      ).postForm(Seq("id" -> "1", "email" -> "brady.thompson@gmail.com"))
        .asString.body
    } should equal ("success")
  }

  "A REST endpoint" should "pass us data" in {
    async {
      Http(
        s"http://$localhost:$port/user/10/email"
      ).asString.body
    } should equal ("success")
  }

  "A REST endpoint" should "consume our parameters" in {
    async {
      Http(
        s"http://$localhost:$port/user/10/email"
      ).postForm(Seq("email" -> "brady.thompson@gmail.com"))
        .method("PUT").asString.body
    } should equal ("success")
  }
}
