import com.typesafe.sbt.pgp.PgpKeys._
import de.heikoseeberger.sbtheader.license.Apache2_0
import sbtrelease._
import sbtrelease.ReleasePlugin._
import sbtrelease.ReleasePlugin.ReleaseKeys._
import sbtrelease.ReleaseStateTransformations._
//import ScoverageSbtPlugin.ScoverageKeys._
import xerial.sbt.Sonatype.SonatypeKeys._


lazy val parent = project in file(".") dependsOn (
  api, core, reactiveStreams, rxScala, tests) aggregate (
  api, all, core, reactiveStreams, rxScala, tests, guide, examples) settings (
  transducersSettings,
  doNotPublish,
  name := "transducers-scala-parent")

lazy val all = project dependsOn (api, core, reactiveStreams, rxScala) aggregate(api, core, reactiveStreams, rxScala) settings(
  transducersSettings,
  buildsUberJar,
  name := "transducers-scala-all")

lazy val api = project enablePlugins AutomateHeaderPlugin settings(
  transducersSettings,
  name := "transducers-scala-api")

lazy val core = project enablePlugins AutomateHeaderPlugin dependsOn api settings (
  transducersSettings,
  buildsUberJar,
  name := "transducers-scala")

lazy val reactiveStreams = project in file("contrib") / "reactive-streams" enablePlugins AutomateHeaderPlugin dependsOn api settings(
  transducersSettings,
  buildsUberJar,
  name := "transducers-scala-reactivestreams",
  libraryDependencies += "org.reactivestreams" % "reactive-streams" % "1.0.0" % "provided")

lazy val rxScala = project in file("contrib") / "rx-scala" enablePlugins AutomateHeaderPlugin dependsOn api settings(
  transducersSettings,
  buildsUberJar,
  name := "transducers-scala-rxscala",
  libraryDependencies += "io.reactivex" %% "rxscala" % "0.26.0")

lazy val examples = project enablePlugins AutomateHeaderPlugin dependsOn core settings(
  transducersSettings,
  doNotPublish,
  name := "transducers-scala-examples")

lazy val benchmarks = project enablePlugins (AutomateHeaderPlugin, JmhPlugin) dependsOn core settings (
  transducersSettings,
  doNotPublish,
  name := "transducers-scala-bechmarks",
  libraryDependencies ++= List(
    "io.reactivex"      %% "rxscala"          % "0.26.0",
    "org.functionaljava" % "functionaljava"   % "4.5",
    "com.cognitect"      % "transducers-java" % "0.4.67"))

lazy val guide = project enablePlugins (AutomateHeaderPlugin, BuildInfoPlugin) configs IntegrationTest dependsOn (tests % "it->test") settings (
  transducersSettings,
  doNotPublish,
  buildInfos,
  Defaults.itSettings,
  name := "transducers-scala-guide",
  resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
  scalacOptions in IntegrationTest += "-Yrangepos",
  testOptions in IntegrationTest += Tests.Argument("html", "markdown", "console", "all", "html.toc", "html.nostats"),
  parallelExecution in IntegrationTest := false,
  libraryDependencies ++= List(
    "org.specs2" %% "specs2-html" % "3.7.2"  % "it,test"))

lazy val tests = project enablePlugins AutomateHeaderPlugin dependsOn (core, reactiveStreams, rxScala) settings (
  transducersSettings,
  doNotPublish,
  name := "transducers-scala-tests",
  resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
  libraryDependencies ++= List(
    "com.typesafe.akka" %% "akka-stream"       % "2.4.2"  % "test",
    "org.specs2"        %% "specs2-core"       % "3.7.2"  % "test",
    "org.specs2"        %% "specs2-scalacheck" % "3.7.2"  % "test",
    "org.scalacheck"    %% "scalacheck"        % "1.13.0" % "test"))

lazy val dist = project disablePlugins AssemblyPlugin settings (
  scalaVersion := "2.11.8",
  target := baseDirectory.value)

// ====================================================================

lazy val transducersSettings =
  buildSettings ++ commonSettings ++ publishSettings

lazy val buildSettings = List(
        organization := "de.knutwalker",
        scalaVersion := "2.11.8",
  crossScalaVersions := scalaVersion.value :: "2.10.6" :: Nil)

lazy val commonSettings = List(
  scalacOptions ++= List(
    "-deprecation",
    "-encoding",  "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-unchecked",
    "-Xcheckinit",
    "-Xfatal-warnings",
    "-Xfuture",
    "-Xlint",
    "-Yclosure-elim",
    "-Ydead-code",
    "-Yno-adapted-args",
    "-Ywarn-adapted-args",
    "-Ywarn-inaccessible",
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit",
    "-Ywarn-numeric-widen"),
  scalacOptions in Test += "-Yrangepos",
  scalacOptions in (Compile, console) ~= (_ filterNot (x ⇒ x == "-Xfatal-warnings" || x.startsWith("-Ywarn"))),
  shellPrompt := { state ⇒
    val name = Project.extract(state).currentRef.project
    (if (name == "parent") "" else name + " ") + "> "
  },
  coverageExcludedPackages := "scalax.transducers.benchmark.*|scalax.transducers.contrib.*|buildinfo",
  headers := {
    val thisYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val years = List(startYear.value.getOrElse(thisYear), thisYear).distinct.mkString(" – ")
    Map(
      "java"  -> Apache2_0(years, maintainer.value),
      "scala" -> Apache2_0(years, maintainer.value))
  },
  initialCommands in      console := """import scalax.transducers._""",
  initialCommands in consoleQuick := "",
  fork in test := true,
  logBuffered := false)

lazy val publishSettings = releaseSettings ++ sonatypeSettings ++ List(
                 startYear := Some(2014),
         publishMavenStyle := true,
   publishArtifact in Test := false,
      pomIncludeRepository := { _ => false },
                maintainer := "Paul Horn",
                githubUser := "knutwalker",
                githubRepo := "transducers-scala",
                  homepage := Some(url(s"https://github.com/${githubUser.value}/${githubRepo.value}")),
                  licenses := List("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
                   scmInfo := _scmInfo(githubUser.value, githubRepo.value),
               tagComment <<= version map (v => s"Release version $v"),
            commitMessage <<= version map (v => s"Set version to $v"),
               versionBump := sbtrelease.Version.Bump.Minor,
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
        <id>{githubUser.value}</id>
        <name>{maintainer.value}</name>
        <url>http://knutwalker.de/</url>
      </developer>
    </developers>
  },
  pomPostProcess := { (node) =>
    val rewriteRule = new scala.xml.transform.RewriteRule {
      override def transform(n: scala.xml.Node): scala.xml.NodeSeq =
        if (n.label == "dependency" && (n \ "groupId").text == "org.scoverage")
          scala.xml.NodeSeq.Empty
        else n
    }
    val transformer = new scala.xml.transform.RuleTransformer(rewriteRule)
    transformer.transform(node).head
  },
  releaseProcess := List[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    runIntegrationTest,
    commitReleaseVersion,
    tagRelease,
    publishSignedArtifacts,
    releaseToCentral,
    setNextVersion,
    commitNextVersion,
    pushChanges,
    publishArtifacts
  ))

lazy val doNotPublish = List(
          publish := (),
     publishLocal := (),
  publishArtifact := false)

lazy val buildInfos = List(
     buildInfoKeys := List[BuildInfoKey](
       organization,
       name in core,
            version,
       scalaVersion,
  BuildInfoKey("dependencies" → (libraryDependencies.in(core).value ++ libraryDependencies.in(api).value).distinct),
  BuildInfoKey("modules" → List(
    (name in reactiveStreams).value → (libraryDependencies in reactiveStreams).value,
            (name in rxScala).value → (libraryDependencies in rxScala).value))))

lazy val buildsUberJar = List(
     assemblyJarName in assembly := s"${name.value}_${scalaBinaryVersion.value}-${version.value}.jar",
  assemblyOutputPath in assembly := (target in dist).value / (assemblyJarName in assembly).value,
      assemblyOption in assembly ~= { _.copy(includeScala = false, includeDependency = false) })

// ====================================================================

lazy val maintainer = SettingKey[String]("Maintainer")
lazy val githubUser = SettingKey[String]("Github username")
lazy val githubRepo = SettingKey[String]("Github repository")

lazy val runIntegrationTest = ReleaseStep(
  action = { state =>
    val shouldSkipTests = state get skipTests getOrElse false
    if (!shouldSkipTests) {
      val extracted = Project extract state
      val ref = extracted get thisProjectRef
      extracted.runAggregated(test in IntegrationTest in ref, state)
    } else state
  },
  enableCrossBuild = true)

lazy val publishSignedArtifacts = publishArtifacts.copy(
  action = { state =>
    val extracted = Project extract state
    val ref = extracted get thisProjectRef
    extracted.runAggregated(publishSigned in Global in ref, state)
  },
  enableCrossBuild = true)

lazy val releaseToCentral = ReleaseStep(
  action = { state =>
    val extracted = Project extract state
    val ref = extracted get thisProjectRef
    extracted.runAggregated(sonatypeReleaseAll in Global in ref, state)
  },
  enableCrossBuild = true)

def _scmInfo(user: String, repo: String) = Some(ScmInfo(
  url(s"https://github.com/$user/$repo"),
  s"scm:git:https://github.com/$user/$repo.git",
  Some(s"scm:git:ssh://git@github.com:$user/$repo.git")
))

addCommandAlias("travis", ";clean;coverage;testOnly -- xonly exclude contrib;coverageReport;coverageAggregate")
