package cortex.controller

import cortex.controller.ContentType._
import cortex.model.{Primitive, ActionContext, Request}

/**
  * Created by jasonflax on 2/19/16.
  */
case class Action(handler: (Request) => Message[_],
                  contentType: ContentType,
                  actionContext: ActionContext,
                  methods: Seq[HttpVerb[_ <: Primitive[_]]])