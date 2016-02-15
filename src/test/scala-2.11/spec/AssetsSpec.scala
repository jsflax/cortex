package spec

import java.io.File
import cortex.view.Assets

import scala.io.Source

/**
 */
class AssetsSpec extends BaseSpec {

  object assets extends Assets {
    override def viewDir = new File(
      "src/test/assets/"
    )
  }

  "A dynamic javascript file" should "equal the matching file" in {
    Source.fromFile(
      "src/test/assets/test.js"
    ).map(
        _.toByte
      ).toArray should equal (assets.test())
  }
}
