package cortex.app

import java.io.File

import cortex.controller.Controller
import cortex.db.SqlDB
import cortex.io.{HttpProtocolManager, IOManager}
import cortex.util.log
import cortex.view.View

/**
  * Main application trait. Should be extending by an object.
  * This is the applications equivalent of void main(string: Args[])
  */
trait Cortex {

  /**
    * Abstract val so that inheritor must
    * choose a port for their server to run on.
    *
    * @return port number
    */
  def port: Int

  /**
    * Abstract method so that inheritor must
    * list their sequence of controllers
    *
    * @return seq of controllers
    */
  def controllers: Seq[_ <: Controller]

  /**
    * Abstract method so that inheritor must
    * list their sequence of views
    *
    * @return seq of views
    */
  def views: Seq[_ <: View]

  def ioManagers: Seq[_ <: IOManager] = Seq(new HttpProtocolManager(port))

  /**
    * Config file containing sensitive values and general configuration
    * information
    *
    * @return file path to configuration file
    */
  def configFile: Option[File] = None

  /**
    * Config file containing sensitive values and general configuration
    * information
    *
    * @return dynamic configuration class
    */
  lazy val config: Config = new Config(configFile)

  /**
    * Convenience class to pass around database information.
    *
    * @param host  address of host
    * @param db db name
    * @param user     user name
    * @param password password
    */
  protected case class DBConnection(host: String,
                                    db: String,
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
    try {
      SqlDB.initialize(
        DBConnection.unapply(dbConnection).orNull
      )
    } catch {
      case e: Exception => log e
        s"Failed to initialize SqlDB: ${e.getMessage}"
    }
  }

  def start(): Unit = {
    // run server on initialization of [[App]]
    ioManagers.foreach(io => io.loop())

    // native wait
    this.synchronized {
      wait()
    }
  }

  def shutdown() = {
    ioManagers.foreach(io => io.shutdown())
  }
}
