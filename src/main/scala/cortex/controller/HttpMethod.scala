package cortex.controller

/**
  * List of http 1.1 method specs.
  */
object HttpMethod extends Enumeration {
  type HttpMethod = Value
  val GET, POST, PATCH, DELETE, PUT = Value
}
