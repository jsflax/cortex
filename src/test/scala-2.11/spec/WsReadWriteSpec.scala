package spec


import cortex.io.ws.{WebSocket, WsHandler}

/**
  * Created by jasonflax on 2/13/16.
  */
class WsReadWriteSpec extends BaseSpec {

  val coffeeOnTheRocks = "Coffee, on the rocks"

  val coffeeIpsum =
    """
      |Caffeine percolator grinder con panna, cream, viennese espresso, caffeine
      |at single shot strong, café au lait beans sweet, shop saucer latte,
      |mazagran con panna aged brewed plunger pot milk. Shop trifecta latte
      |saucer galão fair trade white espresso cinnamon, milk aged body ut blue
      |mountain body sweet barista trifecta coffee kopi-luwak. Robust lungo,
      |ristretto mazagran at, cup medium, crema black id extra filter eu
      |cappuccino. Macchiato, fair trade cultivar rich saucer frappuccino,
      |aftertaste crema, half and half sit eu siphon galão grounds.
      |
      |Kopi-luwak saucer id organic single origin dark, as rich coffee steamed
      |crema a lungo cream sit sugar. Irish, crema dripper milk seasonal milk
      |single origin aged brewed filter lungo blue mountain percolator. Blue
      |mountain mazagran variety aroma dripper cortado robusta café au lait
      |frappuccino, redeye rich foam con panna cup café au lait flavour. Skinny
      |rich coffee kopi-luwak a froth mazagran ut blue mountain aftertaste ut,
      |percolator café au lait viennese caramelization doppio aftertaste robusta.
      |
      |Lungo, half and half iced grinder java, frappuccino, caramelization at
      |grounds, café au lait, doppio frappuccino, grounds blue mountain organic
      |barista, fair trade, half and half sugar body flavour robusta. Sit robust
      |froth, cappuccino, flavour pumpkin spice skinny, espresso coffee
      |viennese body, percolator macchiato cortado single origin cup brewed
      |wings, dark con panna dripper id crema. Cappuccino qui galão affogato
      |galão iced affogato, iced wings redeye cinnamon beans mazagran
      |caramelization froth. Mug whipped extra skinny french press sweet saucer
      |seasonal siphon medium whipped, single origin espresso, cream, aroma
      |french press in so carajillo eu single shot filter frappuccino whipped.
      |
      |Macchiato, kopi-luwak organic and single shot, to go cream, instant, in
      |eu ristretto filter, sweet grinder so con panna, a robust trifecta saucer
      |crema. Viennese, single origin roast doppio galão coffee, arabica coffee,
      |plunger pot cream, percolator dark, fair trade half and half black
      |variety cortado in grinder trifecta lungo plunger pot. Half and half a
      |kopi-luwak caffeine ristretto, chicory robust aroma in, body variety
      |brewed, plunger pot, instant extraction sugar roast aromatic. Skinny est
      |robusta robust blue mountain coffee cultivar, roast beans, aroma java
      |filter barista, sit at robust french press white.
    """.stripMargin

  s"A message: $coffeeOnTheRocks" should "be encoded and then decoded" in {
    val handler = new WsHandler(null)(null)

    val bytes = handler.write(coffeeOnTheRocks.getBytes)

    val mask = Seq[Byte](8, 16, 24, 32)

    val masked: Array[Byte] = bytes.slice(2, bytes.length).zipWithIndex.map(
      bi => bi._1 ^ mask(bi._2 % 4)
    ).map(_.toByte)

    new String(
      handler.read(
        (bytes.slice(0, 2) ++ mask ++ masked).toStream
      ).message.get
    ) should equal(coffeeOnTheRocks)
  }
}
