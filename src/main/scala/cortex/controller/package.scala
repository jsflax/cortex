package cortex

import cortex.model.ActionContext

/**
 */
package object controller {
  implicit class WildcardContext(sc: StringContext) {
    def w(implicit args: Symbol*): ActionContext = {
      ActionContext(sc.s(args.map(_ => "(.+)"):_*))(args)
    }
  }
}
