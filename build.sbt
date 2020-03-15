name := "akka-demo"

version := "0.1"

scalaVersion := "2.12.8"
val akkaVersion = "2.5.20"
val akkaHttpVersion = "10.1.7"
val scalaTestVersion = "3.0.5"
//libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.16"

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion

libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion

libraryDependencies += "org.scalatest" %% "scalatest" % scalaTestVersion