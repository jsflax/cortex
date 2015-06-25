package cortex.io

import cortex.controller.Controller
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Main application trait. Should be extending by an object.
 * This is the applications equivalent of void main(string: Args[])
 */
trait Cortex extends App {
  /**
   * Abstract method so that inheritor must
   * choose a port for their server to run on.
   * @return port number
   */
  def port: Int

  /**
   * Abstract method so that inheritor must
   * list their sequence of controllers
   * @return seq of controllers
   */
  def controllers: Seq[_ <: Controller]

  // run server on initialization of [[App]]
  Future {
    new IOManager(port).loop()
  }

  // native wait
  this.synchronized {
    wait()
  }
}
