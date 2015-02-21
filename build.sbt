import com.typesafe.sbt.pgp.PgpKeys._
import JmhKeys._
import sbtrelease._
import sbtrelease.ReleasePlugin._
import sbtrelease.ReleasePlugin.ReleaseKeys._
import sbtrelease.ReleaseStateTransformations._
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

lazy val projectInformation = Map(
  "maintainer" -> "Paul Horn",
   "startYear" -> "2014",
       "years" -> List(2014, java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)).distinct.mkString(" – ")
)

lazy val publishSettings = List(
                  homepage := Some(url("https://github.com/knutwalker/transducers-scala")),
                  licenses := List("Apache License, Verison 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
                 startYear := Some(projectInformation("startYear").toInt),
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
        <name>${projectInformation("maintainer")}</name>
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

lazy val doNotPublish = List(
          publish := (),
     publishLocal := (),
  publishArtifact := false
)

lazy val headerSettings =
  List(headers := Map(
    "scala" -> (
      HeaderPattern.cStyleBlockComment,
      s"""|/*
          | * Copyright ${projectInformation("years")} ${projectInformation("maintainer")}
          | *
          | * Licensed under the Apache License, Version 2.0 (the "License");
          | * you may not use this file except in compliance with the License.
          | * You may obtain a copy of the License at
          | *
          | *     http://www.apache.org/licenses/LICENSE-2.0
          | *
          | * Unless required by applicable law or agreed to in writing, software
          | * distributed under the License is distributed on an "AS IS" BASIS,
          | * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
          | * See the License for the specific language governing permissions and
          | * limitations under the License.
          | */
          |
          |""".stripMargin
    )
  )) ++
  inConfig(Compile)(compileInputs.in(compile) <<= compileInputs.in(compile).dependsOn(createHeaders.in(compile))) ++
  inConfig(Test)(compileInputs.in(compile) <<= compileInputs.in(compile).dependsOn(createHeaders.in(compile)))

lazy val buildsUberJar = List(
        assemblyJarName in assembly := s"${name.value}_${scalaBinaryVersion.value}-${version.value}.jar",
     assemblyOutputPath in assembly := baseDirectory.value / (assemblyJarName in assembly).value,
         assemblyOption in assembly ~= { _.copy(includeScala = false) }
)

lazy val transducersSettings =
  buildSettings ++ commonSettings ++ publishSettings ++ releaseSettings ++ headerSettings

// =========================================

lazy val parent = project.in(file("."))
  .settings(name := "transducers-scala-parent")
  .settings(transducersSettings: _*)
  .settings(doNotPublish: _*)
  .dependsOn(api, core, reactiveStreams, rxScala, tests, examples, benchmarks)
  .aggregate(api, core, reactiveStreams, rxScala, tests, examples, benchmarks)

lazy val all = project
  .settings(name := "transducers-scala-all")
  .settings(transducersSettings: _*)
  .settings(buildsUberJar: _*)
  .aggregate(api, core, reactiveStreams, rxScala)
  .dependsOn(api, core, reactiveStreams, rxScala)

lazy val api = project
  .settings(name := "transducers-scala-api")
  .settings(transducersSettings: _*)

lazy val core = project
  .settings(name := "transducers-scala")
  .settings(transducersSettings: _*)
  .dependsOn(api)

lazy val reactiveStreams = project.in(file("contrib") / "reactive-streams")
  .settings(name := "transducers-scala-reactivestreams")
  .settings(transducersSettings: _*)
  .settings(libraryDependencies ++= List(
    "org.reactivestreams" % "reactive-streams" % "0.4.0"))
  .dependsOn(core)

lazy val rxScala = project.in(file("contrib") / "rx-scala")
  .settings(name := "transducers-scala-rxscala")
  .settings(transducersSettings: _*)
  .settings(libraryDependencies ++= List(
    "io.reactivex" %% "rxscala" % "0.23.1"))
  .dependsOn(core)

lazy val examples = project
  .settings(name := "transducers-scala-examples")
  .settings(transducersSettings: _*)
  .settings(doNotPublish: _*)
  .dependsOn(core)

lazy val benchmarks = project
  .settings(name := "transducers-scala-bechmarks")
  .settings(transducersSettings: _*)
  .settings(doNotPublish: _*)
  .settings(jmhSettings: _*)
  .settings(
    outputTarget in Jmh  := target.value / s"scala-${scalaBinaryVersion.value}",
    libraryDependencies ++= List(
      "org.functionaljava" % "functionaljava"   % "4.3",
      "com.cognitect"      % "transducers-java" % "0.4.67"))
  .dependsOn(core)

lazy val tests = project
  .settings(name := "transducers-scala-tests")
  .settings(transducersSettings: _*)
  .settings(doNotPublish: _*)
  .settings(libraryDependencies ++= List(
    "com.typesafe.akka" %% "akka-stream-experimental" % "0.10-M1",
    "org.specs2"        %% "specs2-scalacheck"        % "2.4.16" ,
    "org.scalacheck"    %% "scalacheck"               % "1.12.2" )
    .map(_ % "test"))
  .dependsOn(core, reactiveStreams, rxScala)
