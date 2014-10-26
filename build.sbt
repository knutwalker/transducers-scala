name := """transducers-scala"""

description := "Transducers for Scala"

organization in ThisBuild := "de.knutwalker"

scalaVersion in ThisBuild := "2.11.2"

crossScalaVersions in ThisBuild := List("2.10.4", "2.11.2")

scalacOptions in ThisBuild := List(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-target:jvm-1.7",
  "-encoding", "UTF-8")

