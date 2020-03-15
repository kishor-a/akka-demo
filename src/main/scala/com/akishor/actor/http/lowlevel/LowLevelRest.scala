package com.akishor.actor.http.lowlevel

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.akishor.actor.http.lowlevel.GuitarDB.{CreateGuitar, FindAllGuitars}
import spray.json._
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future
import akka.pattern.ask

case class Guitar(make:String, model: String)

object GuitarDB{
  case class CreateGuitar(guitar: Guitar)
  case class GuitarCreated(id: Int)
  case class FindGuitar(id: Int)
  case object FindAllGuitars
}
class GuitarDB extends Actor with ActorLogging{
  import com.akishor.actor.http.lowlevel.GuitarDB._

  var guitars: Map[Int, Guitar]  = Map()

  var currentGuitarId = 0

  override def receive: Receive = {
    case FindAllGuitars => sender() ! guitars.values.toList
    case FindGuitar(id) => sender() ! guitars.get(id)
    case CreateGuitar(guitar) =>
      guitars = guitars + (currentGuitarId -> guitar)

      sender() ! GuitarCreated(currentGuitarId)
      currentGuitarId += 1

  }

}

trait GuitarJsonProtocol extends DefaultJsonProtocol {
  implicit val guitarJsonFormat = jsonFormat2(Guitar)
}

object LowLevelRest extends App with GuitarJsonProtocol {

  //Json sample

  val simpleGuitar = Guitar("Fender","Stratocaster")
  println(simpleGuitar.toJson)
  //json sample ends

  implicit val actorSystem = ActorSystem("lowLevelRest")
  implicit val actorMaterializer = ActorMaterializer()
  import actorSystem.dispatcher

  //setup

  val guitarDb = actorSystem.actorOf(Props[GuitarDB], "GuitarDbActor")
  val guitarList = List(
    Guitar("Fender","Startocaster"),
    Guitar("Gibson","Les Paul"),
    Guitar("Martin","Lx1")
  )

  guitarList.foreach(guitar => {
    guitarDb ! CreateGuitar(guitar)
  })

  import scala.concurrent.duration._

  implicit val defaultTimeout = Timeout(2 seconds)

  val asyncRequestHandler:HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/api/guitars"),_,_,_) =>
      val guitarsFuture: Future[List[Guitar]] = ( guitarDb ? FindAllGuitars).mapTo[List[Guitar]]
      guitarsFuture.map((guitars: Seq[Guitar]) => {
        HttpResponse(
          entity = HttpEntity(
            ContentTypes.`application/json`, guitars.toJson.prettyPrint)
        )
      })
    case request: HttpRequest => request.discardEntityBytes()
      Future( HttpResponse(
        StatusCodes.NotFound,
        entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,"Not found"))
      )
  }

  Http().bindAndHandleAsync(asyncRequestHandler,"localhost",8080)

}
