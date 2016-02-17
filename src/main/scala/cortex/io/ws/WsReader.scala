package cortex.io.ws

import cortex.util.log

/**
  * Created by jasonflax on 2/16/16.
  */
object WsReader {
  @inline private def decode(bytes: Stream[Byte], len: Int, offset: Int) = {
    val mask = bytes.slice(2 + offset, 6 + offset)

    val decoded = new Array[Byte](len)
//    bytes.slice(6 + offset, len).map(_ ^ mask ())
    for (i <- 0 until len) {
      decoded(i) = bytes(6 + offset + i) ^ mask(i % 4)
    }

    decoded
  }

  def read(bytes: Stream[Byte]): Array[Byte] = {
    var len = bytes(1) & 0x7f

    len match {
      case n if n <= 125 =>
        decode(bytes, len, 0)
      case 126 =>
        val data = bytes.slice(2, 4)
        len = Array.tabulate(data.length) { i =>
          data(i) << ((data.length - 1 - i) * 8)
        }.reduce(_ | _)
        decode(bytes, len, 2)
      case 127 =>
        val data = bytes.slice(2, 9)
        len = Array.tabulate(data.length) { i =>
          data(i) << ((data.length - 1 - i) * 8)
        }.reduce(_ | _)
        decode(bytes, len, 7)
    }
  }
}
