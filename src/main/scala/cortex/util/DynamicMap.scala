package cortex.util

import scala.language.dynamics

class DynamicMap(map: Map[String, Any]) extends Dynamic {
  def selectDynamic[A](key: String): A = map(key).asInstanceOf[A]
}
