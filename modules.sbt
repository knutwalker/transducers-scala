import JmhKeys._
import scalariform.formatter.preferences._

lazy val root = project.in(file("."))
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
  .settings(scalariformSettings: _*)
  .settings(ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(AlignParameters, true)
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(CompactControlReadability, true)
    .setPreference(DoubleIndentClassDeclaration, true)
    .setPreference(PreserveDanglingCloseParenthesis, true)
    .setPreference(RewriteArrowSymbols, true))
  .settings(libraryDependencies ++= List(
    "org.reactivestreams" % "reactive-streams" % "0.4.0",
    "com.typesafe.akka" %% "akka-stream-experimental" % "0.10-M1" % "test" exclude("org.reactivestreams", "reactive-streams")))

lazy val rxScala = project
  .in(file("contrib") / "rx-scala")
  .dependsOn(root % "test->test;compile->compile")
  .settings(scalariformSettings: _*)
  .settings(ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(AlignParameters, true)
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(CompactControlReadability, true)
    .setPreference(DoubleIndentClassDeclaration, true)
    .setPreference(PreserveDanglingCloseParenthesis, true)
    .setPreference(RewriteArrowSymbols, true))
  .settings(libraryDependencies ++= List(
    "io.reactivex" %% "rxscala" % "0.22.0"))
