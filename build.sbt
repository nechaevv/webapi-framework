name:= "api-framework"

organization := "ru.osfb.webapi"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.7"

val akkaVersion = "2.4.2"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-xml-experimental" % akkaVersion,
  "com.typesafe.play" %% "play-json" % "2.4.6",
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "org.infinispan" % "infinispan-core" % "8.1.2.Final"
)

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"