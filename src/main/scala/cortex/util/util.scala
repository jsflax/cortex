package cortex.util

/**
 */
object util {
  def isValidEmail(email : String): Boolean =
    if (
      """\b[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*\b"""
        .r.findFirstIn(email) == None) {
      false
    } else {
      true
    }
}
