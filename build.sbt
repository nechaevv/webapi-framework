name:= "api-framework"

organization := "ru.osfb.webapi"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
  "com.typesafe.akka" %% "akka-actor" % "2.4.1",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.1",
  "com.typesafe.akka" %% "akka-http-core-experimental" % "2.0.2",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.0.2",
  "com.typesafe.play" %% "play-json" % "2.4.6",
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.infinispan" % "infinispan-core" % "8.1.0.Final"
)

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"