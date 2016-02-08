package spec

import cortex.model.Scope
import spray.json.DefaultJsonProtocol

/**
  * Created by jasonflax on 2/7/16.
  */
class ScopeSpec extends BaseSpec {

  /**
    * Created by jasonflax on 2/7/16.
    */
  case class Commit(commitTime: Long,
                    author: String,
                    message: String)

  object Commit extends DefaultJsonProtocol {
    implicit val bleh = jsonFormat3(Commit.apply)
  }

  "A new class" should "apply scope" in {
    val commit = Commit(100, "auth", "msg")

    import Scope._

    print(new Scope("author", "message", "scooby")
      .scope(commit).add("b" -> 7).value.prettyPrint)
  }
}
