package spec

import java.io.File
import cortex.app.Cortex
import org.scalatest.{FlatSpec, Matchers}

/**
 */
class ConfigSpec extends BaseSpec {

  override def configFile = Some(new File("src/test/config.txt"))

  "A config file" should
    "contain user as cortex_user and pass as cortex_pass" in {
    config.user should equal ("cortex_user")
    config.pass should equal ("cortex_pass")
  }
}
