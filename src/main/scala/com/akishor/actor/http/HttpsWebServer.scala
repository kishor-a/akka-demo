package com.akishor.actor.http

import java.io.InputStream
import java.security.{KeyStore, SecureRandom}

import akka.actor.ActorSystem
import akka.http.scaladsl.ConnectionContext
import akka.http.scaladsl.{Http, HttpsConnectionContext}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}
import akka.http.scaladsl.server.Directives._


object HttpsWebServer extends App {

  implicit val system= ActorSystem();
  implicit val materilizer = ActorMaterializer();
  import system.dispatcher

  //Step 1
  val ks: KeyStore = KeyStore.getInstance("PKCS12")
  val keyStoreFile: InputStream = getClass().getClassLoader
    .getResourceAsStream("keystore.p12")
  //val fileInputStream = new FileInputStream(new File("src/main/resources/keystore.p12"))
  val password = "test".toCharArray
  ks.load(keyStoreFile,password)

  //Step 2 : Initialize Key manager
  val keyManagerFactory = KeyManagerFactory.getInstance("SunX509") // PKI public key infrastructure
  keyManagerFactory.init(ks, password)

  //Step 3 : Initialize trust manager
  val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
  trustManagerFactory.init(ks)

  //Step 4: Initialize an SSL context
  val sslContext: SSLContext = SSLContext.getInstance("TLS")
  sslContext.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers,
    new SecureRandom())

  val httpsConnectionContext: HttpsConnectionContext = ConnectionContext.https(sslContext)

  val simpleRoute =
    path("/home") {
      complete(HttpResponse(StatusCodes.OK, entity = "Hello"))
    }

  Http().bindAndHandle(simpleRoute,"localhost", 8443, httpsConnectionContext)
}
