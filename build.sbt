name := """transducers-scala"""

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.2"

libraryDependencies ++= List(
  "org.scalaz" %% "scalaz-core" % "7.1.0",
  "com.chuusai" %% "shapeless" % "2.0.0",
  "org.typelevel" %% "shapeless-scalaz" % "0.3",
  "org.scalatest" %% "scalatest" % "2.2.2" % "test")

