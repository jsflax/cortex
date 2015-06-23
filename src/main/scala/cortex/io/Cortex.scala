package cortex.io

import cortex.controller.Controller
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait Cortex extends App {
  def port(): Int
  def controllers(): Seq[_ <: Controller]

  controllers().foreach(_.hashCode())

  Future {
    new IOManager(port()).loop()
  }

  this.synchronized {
    wait()
  }
}