package com.akishor.actor.http.highlevel

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentType, ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.akishor.actor.http.lowlevel.GuitarDB.{CreateGuitar, FindAllGuitars, FindGuitar}
import com.akishor.actor.http.lowlevel.{Guitar, GuitarDB, GuitarJsonProtocol}
import akka.pattern.ask
import akka.util.Timeout
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._
import spray.json.DefaultJsonProtocol

object HigheLevelApiExample extends App with GuitarJsonProtocol {

  implicit val system = ActorSystem("HighLevelApiExample")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher
  import akka.http.scaladsl.server.Directives._

  /*
   * GET /api/guitar
   * GET /api/guitar?id=x
   * GET /api/guitar/x
   * GET /api/guitar/inventory
   */

  //Setup

  val guitarDb = system.actorOf(Props[GuitarDB], "GuitarDbActor")
  val guitarList = List(
    Guitar("Fender", "Startocaster"),
    Guitar("Gibson", "Les Paul"),
    Guitar("Martin", "Lx1")
  )

  guitarList.foreach(guitar => {
    guitarDb ! CreateGuitar(guitar)
  })


  implicit val defaultTimeout = Timeout(2 seconds)

  //Implementation
  // /api/guitar

  val guitarRoute: Route =
    path("api" / "guitar") {
      parameter("id".as[Int]) { // /api/guitar?id=x
        id => {
          get {
            val guitarFuture = (guitarDb ? FindGuitar(id)).mapTo[Option[Guitar]]
            val guitarEntity = guitarFuture.map(guitar => {
              HttpEntity(
                ContentTypes.`application/json`,
                guitar.toJson.prettyPrint
              )
            })
            complete(guitarEntity)
          }
        }
      } ~
      get {
        val allGuitarsFuture = (guitarDb ? FindAllGuitars).mapTo[List[Guitar]]
        val entityFuture = allGuitarsFuture.map(
          guitars => {
            HttpEntity(
              ContentTypes.`application/json`,
              guitars.toJson.prettyPrint
            )
          }
        )
        complete(entityFuture)

      }
    } ~
      path("api" / "guitar" / IntNumber) {
        guitarId => {
          get {
            val guitarFuture = (guitarDb ? FindGuitar(guitarId)).mapTo[Guitar]
            val guitarEntity = guitarFuture.map(guitar => {
              HttpEntity(
                ContentTypes.`application/json`,
                guitar.toJson.prettyPrint
              )
            })
            complete(guitarEntity)
          }
        }
      }

  private def toHttpEntity(payload: String) = HttpEntity(
    ContentTypes.`application/json`,
    payload
  )

  val simplifiedGuitarRoute: Route =
    (pathPrefix("api" / "guitar") & get) {
      (parameter("id".as[Int]) | path(IntNumber)) {
        guitarId => {
          val guitarEntity = (guitarDb ? FindGuitar(guitarId))
            .mapTo[Guitar]
            .map(_.toJson.prettyPrint)
            .map(toHttpEntity)
          complete(guitarEntity)
        }
      } ~
        pathEndOrSingleSlash {
          get {
            val entityFuture = (guitarDb ? FindAllGuitars)
              .mapTo[List[Guitar]]
              .map(_.toJson.prettyPrint)
              .map(toHttpEntity)
            complete(entityFuture)
          }
        }
    }

    Http().bindAndHandle(simplifiedGuitarRoute, "localhost", 8080)


}

