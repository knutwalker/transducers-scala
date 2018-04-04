import com.typesafe.sbt.pgp.PgpKeys._
import de.heikoseeberger.sbtheader.license.Apache2_0
import ReleaseTransformations._
import xerial.sbt.Sonatype.SonatypeCommand

lazy val parent = project in file(".") dependsOn (
  api, core, reactiveStreams, rxScala, akkaStream, tests) aggregate (
  api, all, core, reactiveStreams, rxScala, akkaStream, tests, guide, examples) settings (
  transducersSettings,
  doNotPublish,
  name := "transducers-scala-parent")

lazy val all = project dependsOn (api, core, reactiveStreams, rxScala, akkaStream) aggregate(api, core, reactiveStreams, rxScala, akkaStream) settings(
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
  libraryDependencies += "io.reactivex" %% "rxscala" % "0.26.5")

lazy val akkaStream = project in file("contrib") / "akka-stream" enablePlugins AutomateHeaderPlugin dependsOn api settings(
  transducersSettings,
  buildsUberJar,
  name := "transducers-scala-akka-stream",
  libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.4.20")

lazy val examples = project enablePlugins AutomateHeaderPlugin dependsOn core settings(
  transducersSettings,
  doNotPublish,
  name := "transducers-scala-examples")

lazy val benchmarks = project enablePlugins (AutomateHeaderPlugin, JmhPlugin) dependsOn (core, akkaStream) settings (
  transducersSettings,
  doNotPublish,
  name := "transducers-scala-bechmarks",
  resolvers += Resolver.sonatypeRepo("snapshots"),
  libraryDependencies ++= List(
    "io.reactivex"      %% "rxscala"          % "0.26.5",
    "org.functionaljava" % "functionaljava"   % "4.5",
    "com.cognitect"      % "transducers-java" % "0.4.67",
    "co.fs2"            %% "fs2-core"         % "0.10.3",
    "com.typesafe.play" %% "play-iteratees"   % "2.6.1",
    "org.scalaz.stream" %% "scalaz-stream"    % "0.8.6",
    "io.iteratee"       %% "iteratee-core"    % "0.17.0"))

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
    "org.specs2" %% "specs2-html" % "3.9.5"  % "it,test"))

lazy val tests = project enablePlugins AutomateHeaderPlugin dependsOn (core, reactiveStreams, rxScala, akkaStream) settings (
  transducersSettings,
  doNotPublish,
  name := "transducers-scala-tests",
  resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
  libraryDependencies ++= List(
    "com.typesafe.akka" %% "akka-stream"       % "2.4.20"  % "test",
    "org.specs2"        %% "specs2-core"       % "3.9.5"  % "test",
    "org.specs2"        %% "specs2-scalacheck" % "3.9.5"  % "test",
    "org.scalacheck"    %% "scalacheck"        % "1.13.5" % "test"))

lazy val dist = project disablePlugins AssemblyPlugin settings (
  scalaVersion := "2.12.5",
  target := baseDirectory.value)

// ====================================================================

lazy val transducersSettings =
  buildSettings ++ commonSettings ++ publishSettings

lazy val buildSettings = List(
  organization := "de.knutwalker",
  scalaVersion := "2.12.5")

lazy val commonSettings = List(
  scalacOptions ++= List(
    "-deprecation",
    "-explaintypes",
    "-feature",
    "-unchecked",
    "-encoding", "UTF-8",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-language:reflectiveCalls",
    "-Xlint:_",
    "-Xfuture",
    "-Xfatal-warnings",
    "-Yno-adapted-args",
    "-Ywarn-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-extra-implicit",
    "-Ywarn-inaccessible",
    "-Ywarn-infer-any",
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit",
    "-Ywarn-numeric-widen",
    // implicits,params,linted probably too aggressive
    "-Ywarn-unused:imports,patvars,privates,locals,explicits", // implicits,params,linted
    "-Ywarn-value-discard",
    "-target:jvm-1.8",
    "-Xexperimental",
    "-Ydelambdafy:method"),
  scalacOptions in Test ~= (_.filterNot(_ == "-Xfatal-warnings") :+ "-Yrangepos"),
  scalacOptions in (Compile, console) ~= (_ filterNot (x ⇒ x == "-Xfatal-warnings" || x.startsWith("-Ywarn"))),
  shellPrompt := { state ⇒
    val name = Project.extract(state).currentRef.project
    (if (name == "parent") "" else name + " ") + "> "
  },
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

lazy val publishSettings = List(
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
         releaseTagComment := s"Release version ${version.value}",
      releaseCommitMessage := s"Set version to ${version.value}",
        releaseVersionBump := sbtrelease.Version.Bump.Minor,
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
    releaseStepCommand(SonatypeCommand.sonatypeReleaseAll),
    setNextVersion,
    commitNextVersion,
    pushChanges
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
  BuildInfoKey("dependencies" → (libraryDependencies.in(core).value ++ libraryDependencies.in(api).value)
    .distinct.filterNot(_.configurations.exists(_ == "ensime-internal"))),
  BuildInfoKey("modules" → List(
    (name in reactiveStreams).value → (libraryDependencies in reactiveStreams).value.filterNot(_.configurations.exists(_ == "ensime-internal")),
            (name in rxScala).value → (libraryDependencies in rxScala).value.filterNot(_.configurations.exists(_ == "ensime-internal")),
         (name in akkaStream).value → (libraryDependencies in akkaStream).value.filterNot(_.configurations.exists(_ == "ensime-internal"))))))

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
    val shouldSkipTests = state get ReleaseKeys.skipTests getOrElse false
    if (!shouldSkipTests) {
      val extracted = Project extract state
      val ref = extracted get thisProjectRef
      extracted.runAggregated(test in IntegrationTest in ref, state)
    } else state
  },
  enableCrossBuild = false)

lazy val publishSignedArtifacts = publishArtifacts.copy(
  action = { state =>
    val extracted = Project extract state
    val ref = extracted get thisProjectRef
    extracted.runAggregated(publishSigned in Global in ref, state)
  },
  enableCrossBuild = false)

def _scmInfo(user: String, repo: String) = Some(ScmInfo(
  url(s"https://github.com/$user/$repo"),
  s"scm:git:https://github.com/$user/$repo.git",
  Some(s"scm:git:ssh://git@github.com:$user/$repo.git")
))

addCommandAlias("travis", ";clean;testOnly -- xonly exclude contrib")
