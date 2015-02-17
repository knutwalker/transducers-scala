import com.typesafe.sbt.pgp.PgpKeys._
import JmhKeys._
import sbtassembly.AssemblyPlugin.defaultShellScript
import sbtrelease._
import sbtrelease.ReleasePlugin._
import sbtrelease.ReleasePlugin.ReleaseKeys._
import sbtrelease.ReleaseStateTransformations._
import scalariform.formatter.preferences._
import xerial.sbt.Sonatype.SonatypeKeys._

lazy val buildSettings = List(
        organization := "de.knutwalker",
        scalaVersion := "2.11.5",
  crossScalaVersions := "2.11.5" :: "2.10.4" :: Nil
)

lazy val commonSettings = List(
  scalacOptions in ThisBuild ++=
    "-deprecation" ::
    "-encoding" ::  "UTF-8" ::
    "-explaintypes" ::
    "-feature" ::
    "-language:existentials" ::
    "-language:higherKinds" ::
    "-language:implicitConversions" ::
    "-unchecked" ::
    "-Xcheckinit" ::
    "-Xfatal-warnings" ::
    "-Xfuture" ::
    "-Xlint" ::
    "-Yclosure-elim" ::
    "-Ydead-code" ::
    "-Yno-adapted-args" ::
    "-Ywarn-adapted-args" ::
    "-Ywarn-inaccessible" ::
    "-Ywarn-nullary-override" ::
    "-Ywarn-nullary-unit" ::
    "-Ywarn-numeric-widen" :: Nil,
  scmInfo := Some(ScmInfo(
    url("https://github.com/knutwalker/transducers-scala"),
    "scm:git:https://github.com/knutwalker/transducers-scala.git",
    Some("scm:git:ssh://git@github.com:knutwalker/transducers-scala.git")))
)

lazy val publishSettings = List(
                  homepage := Some(url("https://github.com/knutwalker/transducers-scala")),
                  licenses := List("Apache License, Verison 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
                 startYear := Some(2014),
         publishMavenStyle := true,
   publishArtifact in Test := false,
      pomIncludeRepository := { _ => false },
  SonatypeKeys.profileName := "knutwalker",
               tagComment <<= (Keys.version in ThisBuild) map (v => s"Release version $v"),
            commitMessage <<= (Keys.version in ThisBuild) map (v => s"Set version to $v"),
               versionBump := sbtrelease.Version.Bump.Bugfix,

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra := {
    <developers>
      <developer>
        <id>knutwalker</id>
        <name>Paul Horn</name>
        <url>http://knutwalker.de/</url>
      </developer>
    </developers>
  },
  releaseProcess := List[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    setReleaseVersion,
    runClean,
    runTest,
    commitReleaseVersion,
    tagRelease,
    publishSignedArtifacts,
    releaseToCentral,
    setNextVersion,
    commitNextVersion,
    pushChanges,
    publishArtifacts
  )
)

lazy val publishSignedArtifacts = publishArtifacts.copy(
  action = { st: State =>
    val extracted = Project.extract(st)
    val ref = extracted.get(Keys.thisProjectRef)
    extracted.runAggregated(publishSigned in Global in ref, st)
  },
  enableCrossBuild = true
)

lazy val releaseToCentral = ReleaseStep(
  action = { st: State =>
    val extracted = Project.extract(st)
    val ref = extracted.get(Keys.thisProjectRef)
    extracted.runAggregated(sonatypeReleaseAll in Global in ref, st)
  },
  enableCrossBuild = true
)

lazy val noPublishSettings = List(
          publish := (),
     publishLocal := (),
  publishArtifact := false
)

lazy val formatterSettings = scalariformSettings ++ List(
  ScalariformKeys.preferences := ScalariformKeys.preferences.value.
    setPreference(AlignParameters, true).
    setPreference(AlignSingleLineCaseStatements, true).
    setPreference(CompactControlReadability, true).
    setPreference(DoubleIndentClassDeclaration, true).
    setPreference(PreserveDanglingCloseParenthesis, true).
    setPreference(RewriteArrowSymbols, true)
)

lazy val assembleSettings = List(
        assemblyJarName in assembly := s"${name.value}_${scalaBinaryVersion.value}-${version.value}.jar",
     assemblyOutputPath in assembly := baseDirectory.value / (assemblyJarName in assembly).value,
         assemblyOption in assembly ~= { _.copy(includeScala = false) },
  assemblyMergeStrategy in assembly := {
    // case PathList("META-INF", "CHANGES.txt") => MergeStrategy.rename
    // case PathList("META-INF", "LICENSE.txt") => MergeStrategy.rename
    // case "CHANGES.txt" | "LICENSE.txt"   => MergeStrategy.rename
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
)

lazy val transducersSettings =
  buildSettings     ++ commonSettings   ++
  publishSettings   ++ releaseSettings  ++
  formatterSettings ++ assembleSettings

// =========================================

lazy val rxStreamsDeps = List(
  "org.reactivestreams" % "reactive-streams"         % "0.4.0"
)

lazy val rxScalaDeps = List(
  "io.reactivex"       %% "rxscala"                  % "0.23.1"
)

lazy val benchmarkDeps = List(
  "org.functionaljava"  % "functionaljava"           % "4.3",
  "com.cognitect"       % "transducers-java"         % "0.4.67")

lazy val testDeps = List(
  "org.scalatest"      %% "scalatest"                % "2.2.4"   % "test",
  "com.typesafe.akka"  %% "akka-stream-experimental" % "0.10-M1" % "test" exclude("org.reactivestreams", "reactive-streams") exclude("com.typesafe.akka", "akka-persistence-experimental_2.10") exclude("com.typesafe.akka", "akka-persistence-experimental_2.11")
  // "com.typesafe.akka"  %% "akka-stream-experimental" % "1.0-M3"  % "test" exclude("org.reactivestreams", "reactive-streams") exclude("com.typesafe.akka", "akka-persistence-experimental_2.10") exclude("com.typesafe.akka", "akka-persistence-experimental_2.11")
)

// =========================================

lazy val parent = project.in(file("."))
  .settings(name := "transducers-scala-parent")
  .settings(transducersSettings: _*)
  .settings(noPublishSettings: _*)
  .dependsOn(core, reactiveStreams, rxScala, tests, examples, benchmarks)
  .aggregate(core, reactiveStreams, rxScala, tests, examples, benchmarks)

lazy val all = project
  .settings(name := "transducers-scala-all")
  .settings(transducersSettings: _*)
  .aggregate(core, reactiveStreams, rxScala)
  .dependsOn(core, reactiveStreams, rxScala)

lazy val core = project
  .settings(name := "transducers-scala")
  .settings(transducersSettings: _*)

lazy val reactiveStreams = project.in(file("contrib") / "reactive-streams")
  .settings(name := "transducers-scala-reactivestreams")
  .settings(transducersSettings: _*)
  .settings(libraryDependencies ++= rxStreamsDeps)
  .dependsOn(core)

lazy val rxScala = project.in(file("contrib") / "rx-scala")
  .settings(name := "transducers-scala-rxscala")
  .settings(transducersSettings: _*)
  .settings(libraryDependencies ++= rxScalaDeps)
  .dependsOn(core)

lazy val tests = project
  .settings(name := "transducers-scala-tests")
  .settings(transducersSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(libraryDependencies ++= testDeps)
  .dependsOn(core, reactiveStreams, rxScala)

lazy val examples = project
  .settings(name := "transducers-scala-examples")
  .settings(transducersSettings: _*)
  .settings(noPublishSettings: _*)
  .dependsOn(core)

lazy val benchmarks = project.dependsOn(core)
  .settings(name := "transducers-scala-bechmarks")
  .settings(transducersSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(jmhSettings: _*)
  .settings(
    outputTarget in Jmh  := target.value / s"scala-${scalaBinaryVersion.value}",
    libraryDependencies ++= benchmarkDeps)
