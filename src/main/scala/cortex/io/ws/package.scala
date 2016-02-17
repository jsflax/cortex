package cortex.io

import scala.language.implicitConversions

/**
  * Created by jasonflax on 2/16/16.
  */
package object ws {
  implicit private [ws] def intToByte(int: Int): Byte = int.toByte
}
