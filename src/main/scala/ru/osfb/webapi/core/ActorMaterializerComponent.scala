package ru.osfb.webapi.core

import akka.stream.ActorMaterializer

/**
  * Created by v.a.nechaev on 20.01.2016.
  */
trait ActorMaterializerComponent {
  implicit def actorMaterializer: ActorMaterializer
}

trait ActorMaterializerComponentImpl extends ActorMaterializerComponent { this: ActorSystemComponent =>
  override implicit lazy val actorMaterializer: ActorMaterializer = ActorMaterializer()
}
