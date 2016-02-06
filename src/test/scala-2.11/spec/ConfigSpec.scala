package spec

import java.io.File

import cortex.io.Cortex
import cortex.util.test
import org.scalatest.{FlatSpec, Matchers}

/**
 */
class ConfigSpec extends BaseSpec {

  @test object app extends Cortex {
    override def port = 9996

    override def controllers = Seq()
    override def views = Seq()
    override def configFile = Some(new File("src/test/config.txt"))
  }

  app.hashCode()

  "A config file" should "contain user as cortex_user and pass as cortex_pass" in {
    app.config.user should equal ("cortex_user")
    app.config.pass should equal ("cortex_pass")
  }
}
