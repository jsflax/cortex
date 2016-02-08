package cortex.model

import spray.json._

/**
  * Class with the purpose of scoping our domains, e.g.:
  *
  * case class Bicycle(wheels: Seq[Wheel],
  *                    handlebars: (Handlebar, Handlebar),
  *                    pedals: (Pedal, Pedal),
  *                    frame: Frame)
  *
  * http://api.iheartbicycles.com/bicycle/20?scope=frame,pedals
  *
  * Adding the scope variable means that we would only get back those
  * two fields, e.g.,:
  *
  * {
  *   "frame": {...},
  *   "pedals": [{...}, {...}]
  * }
  *
  * No scope parameter means all fields.
  *
  * @param fields fields to filter in from domain
  */
case class Scope(fields: String*) {
  var value: JsObject = null

  /**
    * Add additional fields to the scope if need be.
    * @param additionalFields additional fields to add to scope
    * @tparam A json formattable fields
    * @return this, to do what you will with the scope
    */
  def add[A: JsonFormat](additionalFields: (String, A)*): Scope = {
    value = JsObject(value.fields ++ additionalFields.flatMap {
      case (k, v) => Some(k -> v.toJson)
      case _ => None
    }.toList)
    this
  }

  /**
    * Filter initial domain
    * @param a object to filter
    * @tparam A of type JsonFormat for serialization
    * @return this, to build into the scope if need be
    */
  def scope[A: JsonFormat](a: A): Scope = {
    value =
      if (fields.isEmpty) {
        a.toJson.asJsObject
      } else {
        JsObject(
          a.toJson.asJsObject.fields.filter(v => fields.contains(v._1))
        )
      }

    this
  }
}
