package cortex.model

/**
 */
case class ActionContext(endpoint: String)(implicit symbols: Seq[Symbol]) {
  val coercedEndpoint =
    if (endpoint.startsWith("/")) {
      endpoint
    } else {
      s"/$endpoint"
    }

  val regex = s"$coercedEndpoint".r

  def map(cs: CharSequence): Map[String, String] = {
    val list = regex.unapplySeq(cs).getOrElse(List())
    (symbols.map(_.name) zip list).toMap
  }
}
