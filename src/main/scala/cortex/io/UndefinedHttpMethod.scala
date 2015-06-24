package cortex.io

/**
 * Exception thrown if an http method is used out of the scope
 * of http 1.1 specifications.
 */
case class UndefinedHttpMethod(message: String) extends Exception(message)