import NativePackagerHelper._

name := """phoenix-core"""

version := "0.0.1"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.4",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.8",
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "com.h2database" % "h2" % "1.3.170",
  "org.scalactic" %% "scalactic" % "2.2.6",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
  "ch.qos.logback" %  "logback-classic" % "1.1.7"
)

enablePlugins(JavaServerAppPackaging)

mainClass in Compile := Some("org.phoenixinitiative.core.Main")

