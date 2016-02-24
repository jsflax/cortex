package cortex.util

import scala.collection.mutable
import scala.language.dynamics

import scala.language.implicitConversions


class DynamicValue(any: Any) extends Dynamic {

  val value = any

  def selectDynamic(key: String): DynamicValue = {
    new DynamicValue(any.asInstanceOf[Map[String, Any]](key))
  }

  def unary_~[A] = any.asInstanceOf[A]
}

class DynamicMap(map: Map[String, Any]) extends Dynamic {

  def remove(key: String) = {
    val map = mutable.Map(this.map.toSeq:_*)
    map.remove(key)
    new DynamicMap(map.toMap)
  }

  def toMap = map

  def selectDynamic(key: String): DynamicValue = new DynamicValue(map(key))
}
