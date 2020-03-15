package com.akishor.actor.http.highlevel

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.ExceptionHandler
import akka.stream.ActorMaterializer
import com.akishor.actor.http.highlevel.RejectionsDemo.simpleRoute

object ExceptionHandling extends App {

  //Exceptions are not aggregated unlike rejections
  implicit val system = ActorSystem("DirectiveBreakdown")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher
  import akka.http.scaladsl.server.Directives._

  val simpleRoute =
    //handleExceptions(customExceptionHandler) { // Can be explicitly specified at any location in tree
      path("api" / "myEndpoint") {
        {
          get {
            throw new RuntimeException("Order could not be fetched")
          } ~
            post {
              parameter("id") {
                id => {
                  if (id.length < 2)
                    throw new NoSuchElementException(s"Length validation failed")
                  complete(StatusCodes.OK)
                }
              }
            }
        }
      }

    //}
  implicit val customExceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: RuntimeException =>complete(StatusCodes.NotFound, e.getMessage)
    case e: IllegalArgumentException => complete(StatusCodes.BadRequest, e.getMessage)
  }

  //if nothing matches, default exception handler gets triggered, 505 Internal Server Error
  Http().bindAndHandle(simpleRoute, "localhost", 8080)

}
