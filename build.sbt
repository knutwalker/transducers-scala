import com.typesafe.sbt.pgp.PgpKeys._
import JmhKeys._
import sbtrelease._
import sbtrelease.ReleasePlugin._
import sbtrelease.ReleasePlugin.ReleaseKeys._
import sbtrelease.ReleaseStateTransformations._
import ScoverageSbtPlugin.ScoverageKeys._
import xerial.sbt.Sonatype.SonatypeKeys._

lazy val githubUser = SettingKey[String]("Github username")
lazy val githubRepo = SettingKey[String]("Github repository")
lazy val projectMaintainer = SettingKey[String]("Maintainer")

lazy val buildSettings = List(
        organization := "de.knutwalker",
   projectMaintainer := "Paul Horn",
          githubUser := "knutwalker",
          githubRepo := "transducers-scala",
        scalaVersion := "2.11.6",
  crossScalaVersions := "2.11.6" :: "2.10.5" :: Nil
)

lazy val commonSettings = List(
  scalacOptions ++=
    "-deprecation" ::
    "-encoding" ::  "UTF-8" ::
    "-explaintypes" ::
    "-feature" ::
    "-language:existentials" ::
    "-language:higherKinds" ::
    "-language:implicitConversions" ::
    "-language:postfixOps" ::
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
  scmInfo <<= (githubUser, githubRepo) { (u, r) ⇒ Some(ScmInfo(
    url(s"https://github.com/$u/$r"),
    s"scm:git:https://github.com/$u/$r.git",
    Some(s"scm:git:ssh://git@github.com:$u/$r.git")
  ))},
  shellPrompt := { state ⇒
    val name = Project.extract(state).currentRef.project
    (if (name == "parent") "" else name + " ") + "> "
  },
  coverageExcludedPackages := "scalax.transducers.benchmark.*"
)

lazy val publishSettings = List(
                 homepage <<= (githubUser, githubRepo) { (u, r) => Some(url(s"https://github.com/$u/$r")) },
                  licenses := List("Apache License, Verison 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
                 startYear := Some(2014),
         publishMavenStyle := true,
   publishArtifact in Test := false,
      pomIncludeRepository := { _ => false },
  SonatypeKeys.profileName := "knutwalker",
               tagComment <<= (version in ThisBuild) map (v => s"Release version $v"),
            commitMessage <<= (version in ThisBuild) map (v => s"Set version to $v"),
               versionBump := sbtrelease.Version.Bump.Bugfix,

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra <<= (githubUser, projectMaintainer) { (u, m) ⇒
    <developers>
      <developer>
        <id>${u}</id>
        <name>${m}</name>
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
  List(headers <<= (projectMaintainer, startYear) { (m, y) ⇒
    val years = List(y.get, java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)).distinct.mkString(" – ")
    val license =
      s"""|/*
          | * Copyright $years $m
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
    Map("java" -> (HeaderPattern.cStyleBlockComment, license),
        "scala" -> (HeaderPattern.cStyleBlockComment, license))
  }) ++
  inConfig(Compile)(compileInputs.in(compile) <<= compileInputs.in(compile).dependsOn(createHeaders.in(compile))) ++
  inConfig(Test)(compileInputs.in(compile) <<= compileInputs.in(compile).dependsOn(createHeaders.in(compile)))

lazy val buildInfos = buildInfoSettings ++ List(
  sourceGenerators in Test <+= buildInfo,
  buildInfoPackage := "buildinfo",
     buildInfoKeys := List[BuildInfoKey](
       organization,
       name in core,
            version,
       scalaVersion,
  BuildInfoKey.map(libraryDependencies in core)                 { case (k, v) ⇒ "deps_core" -> v },
  BuildInfoKey.map(libraryDependencies in api)                  { case (k, v) ⇒  "deps_api" -> v },
  BuildInfoKey.map(name in reactiveStreams)                { case (k, v) ⇒  "name_reactive" -> v },
  BuildInfoKey.map(name in rxScala)                              { case (k, v) ⇒  "name_rx" -> v },
  BuildInfoKey.map(libraryDependencies in reactiveStreams) { case (k, v) ⇒  "deps_reactive" -> v },
  BuildInfoKey.map(libraryDependencies in rxScala)               { case (k, v) ⇒  "deps_rx" -> v }
  )
)

lazy val buildsUberJar = List(
        assemblyJarName in assembly := s"${name.value}_${scalaBinaryVersion.value}-${version.value}.jar",
     assemblyOutputPath in assembly := (baseDirectory in parent).value / (assemblyJarName in assembly).value,
         assemblyOption in assembly ~= { _.copy(includeScala = false) }
)

lazy val transducersSettings =
  buildSettings ++ commonSettings ++ publishSettings ++ releaseSettings ++ headerSettings

// =========================================

lazy val parent = project.in(file("."))
  .settings(name := "transducers-scala-parent")
  .settings(transducersSettings: _*)
  .settings(doNotPublish: _*)
  .dependsOn(api, core, reactiveStreams, rxScala, tests, guide, examples, benchmarks)
  .aggregate(api, core, reactiveStreams, rxScala, tests, guide, examples, benchmarks)

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
    "org.reactivestreams" % "reactive-streams" % "0.4.0" % "provided"))
  .dependsOn(api)

lazy val rxScala = project.in(file("contrib") / "rx-scala")
  .settings(name := "transducers-scala-rxscala")
  .settings(transducersSettings: _*)
  .settings(libraryDependencies ++= List(
    "io.reactivex" %% "rxscala" % "0.23.1"))
  .dependsOn(api)

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

lazy val guide = project
  .settings(name := "transducers-scala-guide")
  .settings(transducersSettings: _*)
  .settings(doNotPublish: _*)
  .settings(buildInfos: _*)
  .settings(
    resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
    libraryDependencies ++= List(
      "org.specs2" %% "specs2-html" % "2.4.16" % "test"))
  .dependsOn(tests % "test->test")

lazy val tests = project
  .settings(name := "transducers-scala-tests")
  .settings(transducersSettings: _*)
  .settings(doNotPublish: _*)
  .settings(libraryDependencies ++= List(
    "com.typesafe.akka" %% "akka-stream-experimental" % "0.10-M1",
    "org.specs2"        %% "specs2-core"              % "2.4.16" ,
    "org.specs2"        %% "specs2-scalacheck"        % "2.4.16" ,
    "org.scalacheck"    %% "scalacheck"               % "1.12.2" )
    .map(_ % "test"))
  .dependsOn(core, reactiveStreams, rxScala)


addCommandAlias("cover", ";clean;coverage;test;coverageReport;coverageAggregate")
addCommandAlias("coverCodacy", ";clean;coverage;test;coverageReport;coverageAggregate;codacyCoverage")
