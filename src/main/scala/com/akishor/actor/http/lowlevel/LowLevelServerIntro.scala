package com.akishor.actor.http.lowlevel

import akka.Done
import akka.actor.ActorSystem
import akka.http.javadsl.model.HttpMethods
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object LowLevelServerIntro extends App {

  private implicit val actorSystem = ActorSystem("LowLevelApi")
  implicit val actorMaterializer = ActorMaterializer()
  //execution context for threads
  import actorSystem.dispatcher

  val serverSource = Http().bind("localhost",8000)

  val connectionSink = Sink.foreach[IncomingConnection](
    connection => {
      println(s"Accepted ${connection.remoteAddress}")
    }
  )

  val serverBindingFuture = serverSource.to(connectionSink).run

  serverBindingFuture.onComplete{
    case Success(binding) => println("Server Binding")
      binding.terminate(2 seconds)
    case Failure(exception) => println(s"Server binding failed.$exception")
  }

  /*
   * Method 1. Synchronously
   */

  val requestHandler: HttpRequest => HttpResponse = {

    case HttpRequest(HttpMethods.GET,_,_,_,_) => HttpResponse(
      StatusCodes.OK,
      entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,"Hello"))

    case request: HttpRequest => request.discardEntityBytes()
      HttpResponse(
        StatusCodes.NotFound,
        entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,"Not found"))
  }

  val httpSyncConnectionHandler = Sink.foreach[IncomingConnection]( connection => {
     connection.
       handleWithSyncHandler(requestHandler);
  })

  Http().bind("localhost",8080).runWith(httpSyncConnectionHandler)

  //Shorthand
  //Http().bindAndHandleSync(requestHandler,"localhost",8080)

  /*
   * Method 2. Asynchronously
   */

  val asyncRequestHandler: HttpRequest => Future[HttpResponse] = {

    case HttpRequest(HttpMethods.GET,Uri.Path("/home"),_,_,_) => Future(HttpResponse(
      StatusCodes.OK,
      entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,"Hello from Async"))
    )

    case request: HttpRequest => request.discardEntityBytes()
      Future( HttpResponse(
        StatusCodes.NotFound,
        entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,"Not found"))
    )
  }

  val httpAsyncConnectionHandler = Sink.foreach[IncomingConnection]( connection => {
    connection.
      handleWithAsyncHandler(asyncRequestHandler);
  })

  Http().bindAndHandleAsync(asyncRequestHandler,"localhost",8081)

  /*
   * Method 3. Via akka streams/flow
   */

  val streamBasedSsyncRequestHandler: Flow[ HttpRequest, HttpResponse, _] = Flow[HttpRequest].map(
    {
      case HttpRequest(HttpMethods.GET,Uri.Path("/home"),_,_,_) => HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,"Hello from Async"))


      case request: HttpRequest => request.discardEntityBytes()
         HttpResponse(
          StatusCodes.NotFound,
          entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,"Not found"))
    }
  )

  /*Http().bind("localhost",8082).runForeach{
    connection =>
      connection.handleWith(streamBasedSsyncRequestHandler)
  }*/

  val bindingFuture = Http().bindAndHandle(streamBasedSsyncRequestHandler,"localhost",8082)

  //Shutdown server
  bindingFuture.flatMap(binding => binding.unbind())
    .onComplete(_=> actorSystem.terminate())
}
