package com.akishor.actor.http.highlevel

import akka.actor.ActorSystem
import akka.http.javadsl.server.{MethodRejection, MissingQueryParamRejection}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Rejection, RejectionHandler}
import akka.stream.ActorMaterializer

object RejectionsDemo extends {


  implicit val system = ActorSystem("DirectiveBreakdown")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher
  import akka.http.scaladsl.server.Directives._

  val simpleRoute =
    path("api" / "myEndpoint") {
      {
        get {
          complete(StatusCodes.OK)
        } ~
          parameter("id") {
            id => {
              complete(StatusCodes.OK)
            }
          }
      }
    }

  val badRequestRejectionHandler: RejectionHandler = { rejections: Seq[Rejection] =>
    Some(complete(StatusCodes.BadRequest))
  }

  val forbiddenRejectionHandler: RejectionHandler = { rejections: Seq[Rejection] =>
    Some(complete(StatusCodes.Forbidden))
  }

  val simpleRouteWithRejectionHandler =
    handleRejections(badRequestRejectionHandler) { // handle rejections from top
      //any rejections popups here or in later tree, above mentioned handler will handle those rejections
      path("api" / "myEndpoint") {
        {
          get {
            complete(StatusCodes.OK)
          } ~
            post {
              handleRejections(forbiddenRejectionHandler) { //handle rejections within
                parameter("id") {
                  id => {
                    complete(StatusCodes.OK)
                  }
                }
              }
            }
        }
      }
    }

  //Http().bindAndHandle(simpleRouteWithRejectionHandler, "localhost", 8080)

  //from the list of rejections, if first matches, it gets kicked in, sequence is important
  implicit val customRejectionHandler = RejectionHandler.newBuilder()
    .handle {
      case m: MissingQueryParamRejection => complete("Query param is missing")
    }
    .handle {
      case mr: MethodRejection => complete("Method Rejected")
    }
    .result()

  //sealing a route

  Http().bindAndHandle(simpleRoute, "localhost", 8080)
}
