package spec

import cortex.util.log
import org.scalatest.{Matchers, FlatSpec}

/**
  * Created by jasonflax on 2/6/16.
  */
trait BaseSpec extends FlatSpec with Matchers {
  log.on = false
}
