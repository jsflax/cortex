package cortex.io

import cortex.controller.Controller
import cortex.db.DB
import cortex.util.log
import cortex.view.View
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

  /**
   * Abstract method so that inheritor must
   * list their sequence of views
   * @return seq of views
   */
  def views: Seq[_ <: View]

  /**
   * Convenience class to pass around database information.
   * @param address address of db
   * @param user user name
   * @param password password
   */
  protected case class DBConnection(address: String,
                                    user: String,
                                    password: String)
  /**
   *
   */
  def dbConnection: DBConnection = null

  // pre-initialize views
  views.foreach(_.hashCode())

  // pre-initialize controllers
  controllers.foreach(_.hashCode())

  // initialize db
  if (dbConnection != null) {
    DB.initialize(
      DBConnection.unapply(dbConnection).orNull
    )
  }

  def singleTestLoop() = Future { new IOManager(port).singleTestLoop() }

  // run server on initialization of [[App]]
  if (!this.getClass.isAnnotationPresent(classOf[cortex.util.test])) {
    Future {
      new IOManager(port).loop()
    }

    // native wait
    this.synchronized {
      wait()
    }
  }
}
