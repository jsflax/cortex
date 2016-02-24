package spec

import java.io.File
import cortex.view.Assets

import scala.io.Source
import scalaj.http.Http

/**
  */
class AssetsSpec extends BaseSpec {

  object assets extends Assets {
    override def viewDir = new File(
      "src/test/assets/"
    )
  }

  views += assets

  "A dynamic javascript file" should "equal the matching file" in {
    Source.fromFile(
      "src/test/assets/test.js"
    ).map(
      _.toByte
    ).toArray should equal(assets.test())
  }

  "A dynamic minified javascript file" should "equal the matching file" in {
    async {
      Http(
        s"http://$localhost:$port/assets/testmin.min.js"
      ).asString.body
    } should equal(
      new String(
        Source.fromFile(
          "src/test/assets/testmin.min.js"
        ).map(
          _.toByte
        ).toArray
      )
    )
  }
}
