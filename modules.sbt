import Common._
import JmhKeys._
import com.typesafe.sbt.pgp.PgpKeys._

lazy val core = project
  .in(file("core"))
  .settings(formatterSettings: _*)
  .settings(
    name := "transducers-scala",
    description := "Transducers for Scala",
    libraryDependencies ++= List(
      "org.scalatest"  %% "scalatest"  % "2.2.2" % "test"))

lazy val reactiveStreams = project
  .in(file("contrib") / "reactive-streams")
  .dependsOn(core % "test->test;compile->compile")
  .settings(formatterSettings: _*)
  .settings(
    name := "transducers-scala-reactivestreams",
    description := "Enable Transducers for Reactive Streams",
    libraryDependencies ++= List(
      "org.reactivestreams" % "reactive-streams" % "0.4.0",
      "com.typesafe.akka" %% "akka-stream-experimental" % "0.10-M1" % "test" exclude("org.reactivestreams", "reactive-streams") exclude("com.typesafe.akka", "akka-persistence-experimental_2.10") exclude("com.typesafe.akka", "akka-persistence-experimental_2.11")))

lazy val rxScala = project
  .in(file("contrib") / "rx-scala")
  .dependsOn(core % "test->test;compile->compile")
  .settings(formatterSettings: _*)
  .settings(
    name := "transducers-scala-rxscala",
    description := "Enable Transducers for RxScala and RxJava",
    libraryDependencies ++= List(
      "io.reactivex" %% "rxscala" % "0.22.0"))

lazy val benchmark = project
  .in(file("benchmark"))
  .dependsOn(core)
  .settings(jmhSettings: _*)
  .settings(
    name := "transducers-scala-benchmark",
    description := "Benchmarks for Scala Transducers",
    outputTarget in Jmh := target.value / s"scala-${scalaBinaryVersion.value}",
    publishSigned := {},
    libraryDependencies ++= List(
      "com.cognitect" % "transducers-java" % "0.4.67"))

lazy val root = project
  .in(file("."))
  .dependsOn(core, reactiveStreams, rxScala)
  .aggregate(core, reactiveStreams, rxScala)
  .settings(signedReleaseSettings: _*)
  .settings(sonatypeSettings: _*)
  .settings(
    name := "transducers-scala-all",
    description := "Transducers for Scala with all additional Modules",
    publishSigned := {})
