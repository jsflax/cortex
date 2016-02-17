package cortex.io.ws

/**
  * Created by jasonflax on 2/16/16.
  */
object WsWriter {
  private def tabulateBytes(frame: Array[Byte],
                            amount: Int,
                            offset: Int,
                            len: Int): Int = {
    Array.tabulate(amount) { i =>
      frame(i + offset) = (len >> ((offset - 1 - i) * 8)) & 255
    }
    amount + offset
  }

  @inline private def encode(bytes: Array[Byte]) = {
    var frameCount = 0
    val frame = new Array[Byte](10)

    frame(0) = 129

    bytes.length match {
      case n if n <= 125 =>
        frame(1) = bytes.length
        frameCount = 2
      case n if n >= 126 && n <= 65535 =>
        frame(1) = 126
        frameCount = tabulateBytes(frame, 2, 2, bytes.length)
      case _ =>
        frame(1) = 127
        frameCount = tabulateBytes(frame, 8, 2, bytes.length)
    }

    frame.slice(0, frameCount) ++ bytes
  }

  def write(message: Array[Byte]) = encode(message)
}
