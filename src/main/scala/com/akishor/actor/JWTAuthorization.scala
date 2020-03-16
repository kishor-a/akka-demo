package com.akishor.actor

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.server.Directives._
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtSprayJson}

import scala.util.{Failure, Success}

case class LoginRequest(userName: String, password: String)
object SecurityDomain extends DefaultJsonProtocol {
  implicit val loginRequestFormat = jsonFormat2(LoginRequest)
}
object JWTAuthorization extends App with SprayJsonSupport {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  import system.dispatcher



  val credentialsDb = Map(
    "user1" -> "pass1",
    "user2" -> "pass2"
  )
  val secretKey = "my secret key"
  val algorithm = JwtAlgorithm.HS256

  def checkPassword(username: String, password: String): Boolean = {
    credentialsDb.contains(username) && credentialsDb(username) == password
  }

  def createToken(username: String, duration: Int): String = {

    val claims = JwtClaim(
      expiration = Some((System.currentTimeMillis() / 1000) + TimeUnit.DAYS.toSeconds(duration)),
      issuedAt = Some(System.currentTimeMillis() / 1000),
      issuer = Some("akishor")
    )

    JwtSprayJson.encode(claims, secretKey, algorithm)
  }


  def isTokenExpired(token: String) : Boolean =
    JwtSprayJson.decode(token, secretKey, Seq(algorithm)) match {
      case Success(claims) => claims.expiration.getOrElse(0L) < System.currentTimeMillis() / 1000
      case Failure(_) => true
    }

  def isTokenValid(token: String) : Boolean = JwtSprayJson.isValid(token, secretKey, Seq(algorithm))

  val loginRoute =
    post {
      entity(as[LoginRequest]) {
        case LoginRequest(username, password) if checkPassword(username, password) =>
          val token = createToken(username, 1)
          respondWithHeader(RawHeader("Access-Token", token)) {
            complete(StatusCodes.OK)
          }
        case _ => complete(StatusCodes.Unauthorized)
      }

    }

  val authenticatedRoute =
    (path("authenticatedApi") & get) {
      optionalHeaderValueByName("Access-Token") {

        //TODO fix this logic
        case Some(token) if isTokenExpired(token) =>
          complete(
            HttpResponse(status = StatusCodes.Unauthorized, entity = "Token Expired."))
        case Some(token) if isTokenValid(token) =>
          complete("Access valid")
        case _ => complete(
          HttpResponse(
            StatusCodes.Unauthorized,
            entity = "Token is invalid of has been tampered with."))
      }
    }

  val route = loginRoute ~ authenticatedRoute

  Http().bindAndHandle(route, "localhost", 8080)
}
