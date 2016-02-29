package cortex.io.ws

import cortex.io.ws.MessageType.MessageType
import cortex.util.log

import scala.concurrent.Future
import scala.language.postfixOps

/**
  * Created by jasonflax on 2/17/16.
  */
// Close codes defined in RFC 6455, section 11.7.
private object CloseCode extends Enumeration {
  type CloseCode = Int

  val CloseNormalClosure = 1000
  val CloseGoingAway = 1001
  val CloseProtocolError = 1002
  val CloseUnsupportedData = 1003
  val CloseNoStatusReceived = 1005
  val CloseAbnormalClosure = 1006
  val CloseInvalidFramePayloadData = 1007
  val ClosePolicyViolation = 1008
  val CloseMessageTooBig = 1009
  val CloseMandatoryExtension = 1010
  val CloseInternalServerErr = 1011
  val CloseTLSHandshake = 1015
}

// The message types are defined in RFC 6455, section 11.8.
object MessageType extends Enumeration {
  type MessageType = Int
  val ContinuationFrame = 0
  // TextMessage denotes a text data message. The text message payload is
  // interpreted as UTF-8 encoded text data.
  val TextMessage = 1

  // BinaryMessage denotes a binary data message.
  val BinaryMessage = 2

  // CloseMessage denotes a close control message. The optional message
  // payload contains a numeric code and text. Use the FormatCloseMessage
  // function to format a close message payload.
  val CloseMessage = 8

  // PingMessage denotes a ping control message. The optional message payload
  // is UTF-8 encoded text.
  val PingMessage = 9

  // PongMessage denotes a ping control message. The optional message payload
  // is UTF-8 encoded text.
  val PongMessage = 10
}

case class Frame(isFinal: Boolean,
                 messageType: MessageType,
                 message: Option[Array[Byte]])

import MessageType._
import CloseCode._

class WsHandler[A](socket: WebSocket[A])
                  (messageReceivedListener: (WebSocket[A], Array[Byte]) => Unit) {

  def read(bytes: Stream[Byte]) = WsReader.readFrame(bytes)

  def write(message: Array[Byte]) = WsWriter.writeFrame(
    Frame(
      isFinal = true,
      messageType = TextMessage,
      message = Some(message)
    )
  )

  /**
    * Created by jasonflax on 2/16/16.
    */
  private object WsReader {
    val FinalBit = 1 << 7

    @inline private def decode(bytes: Stream[Byte], len: Int, offset: Int) = {
      val mask = bytes.slice(2 + offset, 6 + offset)

      Array.tabulate(len) { i => (bytes(6 + offset + i) ^ mask(i % 4)).toByte }
    }

    def readFrame(bytes: Stream[Byte]): Frame = {
      println("Reading Frame")

      val isFinal = (bytes.head & FinalBit) != 0
      val opCode: MessageType = bytes.head & 0xf

      println(s"FIN=$isFinal")
      println(s"OpCode $opCode")

      opCode match {
        case TextMessage | BinaryMessage | PingMessage | PongMessage =>
          var len = bytes(1) & 127.toByte

          Frame(
            isFinal = isFinal,
            messageType = opCode,
            message = Some(
              len match {
                case n if n <= 125 =>
                  decode(bytes, len, 0)
                case 126 =>
                  val data = bytes.slice(2, 4)
                  len = Array.tabulate(data.length) { i =>
                    0xff & data(i) << ((data.length - 1 - i) * 8)
                  }.reduce(_ | _)
                  log e s"actual len: $len"
                  decode(bytes, len, 2)
                case 127 =>
                  log t "decoding 127b msg"
                  val data = bytes.slice(2, 9)
                  len = Array.tabulate(data.length) { i =>
                    0xff & data(i) << ((data.length - 1 - i) * 8)
                  }.reduce(_ | _)
                  decode(bytes, len, 7)
              }
            )
          )
        case CloseMessage =>
          Frame(isFinal, opCode, None)
        case _ =>
          Frame(isFinal = true, opCode, None)
      }
    }
  }

  /**
    * Created by jasonflax on 2/16/16.
    */
  private object WsWriter {
    private def tabulateBytes(frame: Array[Byte],
                              amount: Int,
                              offset: Int,
                              len: Int): Int = {
      Array.tabulate(amount) { i =>
        frame(i + offset) = ((len >> ((offset - 1 - i) * 8)) & 255).toByte
        println(offset - 1 - i)
      }
      amount + offset
    }

    @inline private def encode(bytes: Array[Byte]) = {
      var frameCount = 0
      val frame = new Array[Byte](10)

      frame(0) = 129.toByte

      bytes.length match {
        case n if n <= 125 =>
          frame(1) = bytes.length.toByte
          frameCount = 2
        case n if n >= 126 && n <= 65535 =>
          frame(1) = 126
          frameCount = tabulateBytes(frame, amount = 2, offset = 2, bytes.length)
        case _ =>
          frame(1) = 127
          frameCount = tabulateBytes(frame, 8, 2, bytes.length)
      }

      frame.slice(0, frameCount) ++ bytes
    }

    def writeFrame(frame: Frame) = encode(frame.message.get)
  }

  private[ws] def listen() = {
    if (socket != null) {
      while (socket.isConnected) {

        val frame = WsReader.readFrame(
          Stream.continually(socket.getInputStream.read).map(_.toByte)
        )

        frame.messageType match {
          case TextMessage | BinaryMessage =>
            if (frame.isFinal) {
              log.e(s"Writing Text or Binary Message")
              messageReceivedListener(
                socket,
                frame.message.get
              )
            } else {
              println("Frame not final!")
            }
          case CloseMessage =>
            log.e(s"Writing Close, code: $CloseMessage")
            socket.getOutputStream.write(
              WsWriter.writeFrame(
                Frame(
                  isFinal = true,
                  messageType = CloseMessage,
                  message = Some(
                    s"websocket: close $CloseNormalClosure (normal)".getBytes
                  )
                )
              )
            )
            socket.close()
          case ContinuationFrame =>
            if (!frame.isFinal) {

            }
          case PingMessage | PongMessage =>
            log.e(s"Writing Ping or Pong, code: $PongMessage")
            WsWriter.writeFrame(frame)
        }
      }
      log e "Socket Disconnected!"
    }
  }
}
