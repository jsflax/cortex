import java.io.File

import cortex.controller.Controller
import cortex.controller.Controller.HttpMethod
import cortex.io.Cortex
import cortex.util.test
import cortex.view.View
import org.scalatest.{Matchers, FlatSpec}

import scala.io.Source
import scalaj.http.Http

/**
 */
class ViewSpec extends FlatSpec with Matchers {

  object view extends View {
    override def viewDir = new File(
      "/Users/jason/git/cortex/src/test/resources/"
    )
  }

  object controller extends Controller {
      register("/", { resp =>
        Option(view.dashboard().toString)
      }, HttpMethod.GET)
  }

  @test object app extends Cortex {
    override def port = 9999

    override def controllers = Seq(controller)
    override def views = Seq(view)
  }

  app.hashCode()

  "A dynamic view" should "equal the matching file" in {
    Source.fromFile(
      "/Users/jason/git/cortex/src/test/resources/dashboard.html"
    ).getLines().mkString("\n") should equal (view.dashboard().toString)
  }

  "A controller" should "respond with an html view" in {
    Http(
      "http://localhost:9999"
    ).asString.body should equal (view.dashboard().toString)
  }
}
