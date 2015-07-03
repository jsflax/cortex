import cortex.model.ActionContext

/**
 */
package object spec {
  implicit class WildcardContext(sc: StringContext) {
    def w(implicit args: Symbol*): ActionContext = {
      ActionContext(sc.s(args.map(_ => "(.+)"):_*))(args)
    }
  }
}
