import cortex.controller.Controller
import cortex.controller.Controller.HttpMethod
import cortex.io.Cortex
import cortex.util.{log, test}
import org.scalatest.{Matchers, FlatSpec}

import scalaj.http.Http

/**
 */
class ControllerSpec extends FlatSpec with Matchers {
  object controller extends Controller {
    register("/hello", { resp =>
      Option("Hello world")
    }, HttpMethod.GET)

    register("/sum", { resp =>
      val one: String = resp.params("one")
      val two: String = resp.params("two")

      assert(one forall Character.isDigit)
      assert(two forall Character.isDigit)

      Option((one.toInt + two.toInt).toString)
    }, HttpMethod.GET)
  }

  @test object app extends Cortex {
    override def port = 9998

    override def controllers = Seq(controller)
    override def views = Seq()
  }

  "An endpoint" should "respond with 'Hello world'" in {
    app.singleTestLoop()

    Http(
      "http://localhost:9998/hello"
    ).asString.body should equal ("Hello world")
  }

  "An endpoint" should "calculate the sum of the following numbers" in {
    app.singleTestLoop()

    Http(
      "http://localhost:9998/sum"
    ).params(Seq("one" -> "1", "two" -> "2")).asString.body should equal ("3")
  }
}
