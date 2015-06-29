import cortex.controller.Controller
import cortex.io.Cortex
import cortex.util.{log, test}
import org.scalatest.{Matchers, FlatSpec}

import scalaj.http.Http

import cortex.controller.ContentType._
import cortex.controller.HttpMethod._
/**
 */
class ControllerSpec extends FlatSpec with Matchers {
  object controller extends Controller {
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
  }

  @test object app extends Cortex {
    override def port = 9998

    override def controllers = Seq(controller)
    override def views = Seq()
  }

  "An endpoint" should "respond to a GET with 'Hello world'" in {
    app.singleTestLoop()

    Http(
      "http://localhost:9998/hello"
    ).asString.body should equal ("Hello world")
  }

  "An endpoint" should "GET the sum of the following numbers" in {
    app.singleTestLoop()

    Http(
      "http://localhost:9998/sum"
    ).params(Seq("one" -> "1", "two" -> "2")).asString.body should equal ("3")
  }

  "An endpoint" should "POST and consume our parameters" in {
    app.singleTestLoop()

    Http(
      "http://localhost:9998/updateEmail"
    ).postForm(Seq("id" -> "1", "email" -> "brady.thompson@gmail.com"))
     .asString.body should equal ("success")
  }
}
