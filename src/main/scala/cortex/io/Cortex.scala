package cortex.io

import cortex.controller.Controller
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait Cortex extends App {
  /**
   * Abstract method so that inheritor must
   * choose a port for their server to run on.
   * @return port number
   */
  abstract def port(): Int

  /**
   * Abstract method so that inheritor must
   * list their sequence of controllers
   * @return seq of controllers
   */
  abstract def controllers(): Seq[_ <: Controller]

  // initialize controllers TODO: see if necessary
  controllers().foreach(_.hashCode())

  // run server on initialization of [[App]]
  Future {
    new IOManager(port()).loop()
  }

  // native wait
  this.synchronized {
    wait()
  }
}