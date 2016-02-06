package cortex.io

import java.io.File

import cortex.controller.Controller
import cortex.db.DB
import cortex.view.View
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import scala.language.dynamics

/**
  * Main application trait. Should be extending by an object.
  * This is the applications equivalent of void main(string: Args[])
  */
trait Cortex extends App {

  class Config(file: Option[File]) extends Dynamic {

    if (file.isEmpty) {
      throw new Exception("Config file has not been set.")
    }

    /**
      * Declare partial function type to be added
      * to our map (allows for all generic methods).
      */
    type GenFn = PartialFunction[Seq[Any], Array[Byte]]

    /** Dynamic method storage */
    // create a dynamic method for each key value pair so that the
    // consumer can have easy access, e.g. config.username()
    // TODO: cache file data as strings
    protected lazy val fields: Map[String, String] = {
      scala.io.Source.fromFile(file.get).getLines().map { unsplitKvp =>
        val splitKvp = unsplitKvp.split("=")
        updateDynamic(splitKvp(0))(splitKvp(1))
      }.toMap.withDefault { key => throw new NoSuchFieldError(key) }
    }


    def selectDynamic(key: String) = fields(key)

    def updateDynamic(key: String)(value: String) = key -> value

    def applyDynamic(key: String)(args: (Any, Any)*): String = fields(key)
    def applyDynamicNamed(name: String)(args: (String, Any)*) = fields(name)
  }

  /**
    * Abstract method so that inheritor must
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
    * @param address  address of db
    * @param user     user name
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

  def singleTestLoop() = Future {
    new IOManager(port).singleTestLoop()
  }

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
