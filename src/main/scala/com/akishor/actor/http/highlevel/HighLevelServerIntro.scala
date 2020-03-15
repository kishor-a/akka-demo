package com.akishor.actor.http.highlevel

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directive, Route}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

object HighLevelServerIntro extends App{

  implicit val actorSystem = ActorSystem("myServer")
  implicit val actorMaterializer = ActorMaterializer()
  import  actorSystem.dispatcher

  val simpleDirective: Route =
    path("/home") { //Directive
      complete(StatusCodes.OK) //Directive
    }

  // Chaining directives with ~ tilda

  val chainedRoute = path("myPath") {
    get {  //Directive
      complete(StatusCodes.OK)
    } ~
    post {
      complete(StatusCodes.Forbidden)
    }
  } ~
  path("anotherPath"){
    complete((StatusCodes.OK))
  } //Routing Tree

  //Route can be implicitly converted to Flow
  Http().bindAndHandle(simpleDirective,"localhost",8080)

}
