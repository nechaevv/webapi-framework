name:= "api-framework"

organization := "ru.osfb.webapi"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.1",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.1",
  "com.typesafe.akka" %% "akka-http-core-experimental" % "2.0.2",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.0.2",
  "ch.qos.logback" % "logback-classic" % "1.1.3"
)

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"