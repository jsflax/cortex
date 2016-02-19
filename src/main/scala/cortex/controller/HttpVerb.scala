package cortex.controller

import cortex.model.Primitive

/**
  * List of http 1.1 method specs.
  */

abstract class HttpVerb[A <: Primitive[_]](parameters: (String, A)*) {
  val params = parameters.toMap
  override def toString = this.getClass.getSimpleName.replace("$", "")
  override def equals(obj: Any): Boolean = obj.toString.equals(toString)
}

object HttpVerb {
  case object GET extends HttpVerb {
    def apply(parameters: (String, Primitive[_])*) = {
      new GET(parameters:_*)
    }
  }
  case object POST extends HttpVerb {
    def apply(parameters: (String, Primitive[_])*) = {
      new POST(parameters:_*)
    }
  }
  case object PUT extends HttpVerb {
    def apply(parameters: (String, Primitive[_])*) = {
      new PUT(parameters:_*)
    }
  }
  case object PATCH extends HttpVerb {
    def apply(parameters: (String, Primitive[_])*) = {
      new PATCH(parameters:_*)
    }
  }
  case object DELETE extends HttpVerb {
    def apply(parameters: (String, Primitive[_])*) = {
      new DELETE(parameters:_*)
    }
  }

  val values: Seq[HttpVerb[_ <: Primitive[_]]] = Seq(
    GET,
    POST,
    PUT,
    PATCH,
    DELETE
  )
}

case class GET[A <: Primitive[_]](parameters: (String, A)*)
  extends HttpVerb[A](parameters:_*)

case class POST[A <: Primitive[_]](parameters: (String, A)*)
  extends HttpVerb[A](parameters:_*)

case class PUT[A <: Primitive[_]](parameters: (String, A)*)
  extends HttpVerb[A](parameters:_*)

case class PATCH[A <: Primitive[_]](parameters: (String, A)*)
  extends HttpVerb[A](parameters:_*)

case class DELETE[A <: Primitive[_]](parameters: (String, A)*)
  extends HttpVerb[A](parameters:_*)