name := "akka-demo"

version := "0.1"

scalaVersion := "2.12.8"
val akkaVersion = "2.5.20"
val akkaHttpVersion = "10.1.7"
val scalaTestVersion = "3.0.5"
//libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.16"

libraryDependencies ++= Seq ("com.typesafe.akka" %% "akka-stream" % akkaVersion,
"com.typesafe.akka" %% "akka-http"   % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion,
  "com.pauldijou" %% "jwt-spray-json" % "4.2.0"
)