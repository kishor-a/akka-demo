package com.akishor.actor.http.highlevel

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

object DirectiveBreakdown extends App{

  implicit val system = ActorSystem("DirectiveBreakdown")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher
  import akka.http.scaladsl.server.Directives._

  /*
   * 1. Filtering
   */
  val simpleHttpMethodRoute =
    post {  //get, delete, put, patch, head, options
        complete(StatusCodes.Forbidden)
    }

  val simplePathRoute =
    path("about"){
      complete(
        HttpEntity(ContentTypes.`text/html(UTF-8)`,"Hello")
      )
    }

  val complexPathRoute: Route =
    path("api" / "anotherEndpoint"){
        complete(StatusCodes.OK)
    }

  val dontConsufe =
    path("api/anotherEndpoint") { //api%2FanotherEndpoint
      complete(StatusCodes.OK)
    }

  val pathEndRouet =
    pathEndOrSingleSlash { // match for localhost:8080 and localhost:8080/
      complete(StatusCodes.OK)
    }

  /*
   * 1. Extraction Directive
   */

  val pathExtractionDirective =
    path("api" / "order" / IntNumber) {
      //IntNumber changes the Directive it accepts under this path directive to
      // extraction directive which takes int as argument
      orderId => {
        println(s"get order with id $orderId")
        complete(StatusCodes.OK)
      }
    }

  val pathMultiExtractionDirective =
    path ("api" / "order" / IntNumber / IntNumber) {
       //converts to extraction directive which accepts two argument extraction directive
      (orderId, inventoryId) => {
        println(s"Fetch order $orderId from inventory $inventoryId")
        complete(StatusCodes.OK)
      }
    }

  val queryParamExtractionRoute =
    path ("api" / "item" ) {
      parameter("id".as[Int]) { //default converts to String
            //'id symbols are automatically interned into JVM
        itemId => {
          println(s"Queryparam received is $itemId")
          complete(StatusCodes.OK)
        }
      }
    }

  val extractRequestRoute =
    path("controlEndpoint") {
      extractRequest {
        (request: HttpRequest) => {
          println(s"do something wth request $request")
          complete(StatusCodes.OK)
        }
      }
    }

  /*
   * Type 3 Directive Compact
   */

  val compactSimpleNestedRoute =
    (path("api" / "myEndpoint") & get ) {
      complete(StatusCodes.OK)
    }

  val compactExtractRequestRoute =
    (path("api") & parameter("id") & extractRequest ) {
      (id: String, request: HttpRequest) => {
        println("do something here");
        complete(StatusCodes.OK)
      }

    }

  val combinedExtractionRoute =
    (path(IntNumber) | parameter("id".as[Int])){
      id => {
        println(s"Can be served via path or query param $id")
        complete(StatusCodes.OK)
      }
    }


  /*
   * Tyep 4. Actionable directive
   */

  val completeOkRoute = complete(StatusCodes.OK)

  val failedRoute =
    path("notSupported") {
      failWith(new RuntimeException("Unsupported")) // Completes with HTTP 500
    }

  val routeWithRejection =
    path("home") {
      reject
    } ~
    path("index"){
      completeOkRoute
    }

  Http().bindAndHandle(complexPathRoute,"localhost",8080)
}
