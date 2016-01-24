package ru.osfb.webapi.core

import scala.concurrent.ExecutionContext

/**
 * Created by sgl on 16.05.15.
 */
trait ExecutionContextComponent {
  implicit def executionContext: ExecutionContext
}

trait ActorExecutionContextComponentImpl extends ExecutionContextComponent { this: ActorSystemComponent =>
  override implicit def executionContext: ExecutionContext = actorSystem.dispatcher
}