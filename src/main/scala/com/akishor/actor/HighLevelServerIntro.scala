package com.akishor.actor

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer

object HighLevelServerIntro extends App{

  implicit val actorSystem = ActorSystem("myServer")
  implicit val actorMaterializer = ActorMaterializer()

}
