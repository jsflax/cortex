package spec

import java.io.File

import cortex.controller.{ContentType, Controller, HttpMethod}
import cortex.view.View

import scala.io.Source
import scalaj.http.Http
/**
 */
class ViewSpec extends BaseSpec {
  override def views = Seq(
    new View {
      override def viewDir = new File(
        "src/test/resources/"
      )
    }
  )

  override def controllers = Seq(
    new Controller {
      register("/", { resp =>
        Option(views.head.dashboard())
      }, ContentType.TextHtml, HttpMethod.GET)
    }
  )

  "A dynamic view" should "equal the matching file" in {
    Source.fromFile(
      "src/test/resources/dashboard.html"
    ).getLines().mkString("\n") should equal (
      new String(views.head.dashboard())
    )
  }

  "A controller" should "respond with an html view" in {
    async {
      Http(
        s"http://$localhost:$port/"
      ).asString.body
    } should equal(new String(views.head.dashboard()))
  }
}
