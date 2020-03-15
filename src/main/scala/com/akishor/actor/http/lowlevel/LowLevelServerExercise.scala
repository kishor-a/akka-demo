package com.akishor.actor.http.lowlevel

import akka.actor.ActorSystem
import akka.http.javadsl.model.HttpMethods
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow

object LowLevelServerExercise extends App{

  implicit val system = ActorSystem("lowLevelExercise")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  /*
   * Create http server
   * which replies on front door i.e localhost:8338 with "Welcome"
   * with proper html on /about
   * 404 otherwise
   */

  val streamBasedSsyncRequestHandler: Flow[ HttpRequest, HttpResponse, _] = Flow[HttpRequest].map(
    {
      case HttpRequest(HttpMethods.GET,Uri.Path("/"),_,_,_) => HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,"Welcome"))

      case HttpRequest(HttpMethods.GET, Uri.Path("/about"), _,_,_) =>
        HttpResponse(
          StatusCodes.OK,
          entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,
            """<html>
              |<body>Hello from html page</body>
              |</html>""".stripMargin)
        )

      case request: HttpRequest => request.discardEntityBytes()
        HttpResponse(
          StatusCodes.NotFound,
          entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,"Not found"))
    }
  )

  Http().bind("localhost",8338).runForeach{
    connection =>
      connection.handleWith(streamBasedSsyncRequestHandler)
  }

}
