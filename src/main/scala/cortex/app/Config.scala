package cortex.app

import java.io.File
import scala.language.dynamics

/**
  * Created by jasonflax on 2/14/16.
  */
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
