package spec

import java.io.File

import cortex.io.Cortex
import cortex.util.test
import cortex.view.Assets
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

/**
 */
class AssetsSpec extends BaseSpec {

  object assets extends Assets {
    override def viewDir = new File(
      "src/test/assets/"
    )
  }

  @test object app extends Cortex {
    override def port = 9996

    override def controllers = Seq()
    override def views = Seq()
  }

  app.hashCode()

  "A dynamic javascript file" should "equal the matching file" in {
    Source.fromFile(
      "src/test/assets/test.js"
    ).map(
        _.toByte
      ).toArray should equal (assets.test())
  }
}
