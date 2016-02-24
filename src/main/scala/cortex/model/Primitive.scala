package cortex.model

import spray.json._

import scala.util.{Failure, Success, Try}

/**
  * Created by jasonflax on 2/18/16.
  */
abstract class Primitive[A] {
  def coerce(string: String): Try[A]

  override final def toString = this.getClass.getSimpleName
}

case class PrimitiveCastException(failedString: String, clazz: Primitive[_])
  extends Exception(s"Could not cast $failedString to $clazz")

object Primitive {

  case class nil(value: Null) extends Primitive[Null] {
    def coerce(string: String) = Try(null)
  }

  case object nil extends Primitive[Null] {
    def coerce(string: String) = Try(null)
  }

  case object float extends Primitive[Float] {
    def coerce(string: String) =
      Try(string.toFloat) match {
        case Failure(_) => Failure(
          PrimitiveCastException(string, this)
        )
        case Success(v) => Success(v)
      }
  }

  case object double extends Primitive[Double] {
    def coerce(string: String) =
      Try(string.toDouble) match {
        case Failure(_) => Failure(
          PrimitiveCastException(string, this)
        )
        case Success(v) => Success(v)
      }
  }

  case object long extends Primitive[Long] {
    def coerce(string: String) =
      Try(string.toLong) match {
        case Failure(_) => Failure(
          PrimitiveCastException(string, this)
        )
        case Success(v) => Success(v)
      }
  }

  case object ulong extends Primitive[Long] {
    def coerce(string: String) =
      Try(string.toLong) match {
        case Success(long) if long >= 0 => Success(long)
        case _ => Failure(PrimitiveCastException(string, this))
      }
  }

  case object int extends Primitive[Int] {
    def coerce(string: String) =
      Try(string.toInt) match {
        case Failure(_) => Failure(
          PrimitiveCastException(string, this)
        )
        case Success(v) => Success(v)
      }
  }

  case object uint extends Primitive[Int] {
    def coerce(string: String) =
      Try(string.toInt) match {
        case Success(int) if int >= 0 => Success(int)
        case _ => Failure(PrimitiveCastException(string, this))
      }
  }

  case object bool extends Primitive[Boolean] {
    def coerce(string: String) =
      Try(string.toBoolean) match {
        case Failure(_) => Failure(
          PrimitiveCastException(string, this)
        )
        case Success(v) => Success(v)
      }
  }

  private object mapImpl {
    def coerce(str: String): Try[Map[String, Primitive[_]]] = {
      Try(JsonParser(str).asJsObject.fields) match {
        case Success(fields) =>
          def parseFields(mp: Map[String, JsValue]): Map[String, Primitive[_]] = {
            fields.map {
              case (key: String, value: JsValue) =>
                value match {
                  case JsString(v) => key -> string
                  case JsNumber(v) => key -> double
                  case JsObject(v) => key -> map(parseFields(v))
                  case _ => key -> nil
                }
            }
          }
          Success(parseFields(fields))
        case _ => Failure(PrimitiveCastException(str, map))
      }
    }
  }

  case class map(map: Map[String, Primitive[_]])
    extends Primitive[Map[String, Primitive[_]]] {
    def coerce(str: String) = mapImpl.coerce(str)
  }

  case object map extends Primitive[Map[String, Primitive[_]]] {
    def coerce(str: String) = mapImpl.coerce(str)
  }

  case object string extends Primitive[String] {
    def coerce(string: String) = Try(string.toString)
  }

}
