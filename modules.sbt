import JmhKeys._

lazy val root = project.in(file("."))
  .settings(Common.formatterSettings: _*)
  .settings(libraryDependencies ++= List(
    "org.scalatest"  %% "scalatest"  % "2.2.2" % "test"))

lazy val benchmark = project
  .dependsOn(root)
  .settings(jmhSettings: _*)
  .settings(outputTarget in Jmh := target.value / s"scala-${scalaBinaryVersion.value}")
  .settings(libraryDependencies ++= List(
    "com.cognitect" % "transducers-java" % "0.4.67"))

lazy val reactiveStreams = project
  .in(file("contrib") / "reactive-streams")
  .dependsOn(root % "test->test;compile->compile")
  .settings(
    name := "transducers-scala-reactivestreams",
    description := "Enable Transducers for Reactive Streams")
  .settings(Common.formatterSettings: _*)
  .settings(libraryDependencies ++= List(
    "org.reactivestreams" % "reactive-streams" % "0.4.0",
    "com.typesafe.akka" %% "akka-stream-experimental" % "0.10-M1" % "test" exclude("org.reactivestreams", "reactive-streams") exclude("com.typesafe.akka", "akka-persistence-experimental_2.10") exclude("com.typesafe.akka", "akka-persistence-experimental_2.11")))

lazy val rxScala = project
  .in(file("contrib") / "rx-scala")
  .dependsOn(root % "test->test;compile->compile")
  .settings(name := "transducers-scala-rxscala",
    description := "Enable Transducers for RxScala and RxJava")
  .settings(Common.formatterSettings: _*)
  .settings(libraryDependencies ++= List(
    "io.reactivex" %% "rxscala" % "0.22.0"))
