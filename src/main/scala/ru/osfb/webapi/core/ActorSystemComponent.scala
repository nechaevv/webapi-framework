package ru.osfb.webapi.core

import akka.actor.ActorSystem

/**
  * Created by v.a.nechaev on 20.01.2016.
  */
trait ActorSystemComponent {
  implicit def actorSystem: ActorSystem
}
