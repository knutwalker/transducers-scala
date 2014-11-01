startYear in ThisBuild := Some(2014)

homepage in ThisBuild := Some(url("https://github.com/knutwalker/transducers-scala"))

licenses in ThisBuild += "Apache License, Verison 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")

scmInfo in ThisBuild := Some(ScmInfo(url("https://github.com/knutwalker/transducers-scala"), "scm:git:https://github.com/knutwalker/transducers-scala.git", Some("scm:git:ssh://git@github.com:knutwalker/transducers-scala.git")))

pomExtra in ThisBuild :=
  <developers>
    <developer>
      <id>knutwalker</id>
      <name>Paul Horn</name>
      <url>http://knutwalker.de/</url>
    </developer>
  </developers>

publishMavenStyle in ThisBuild := true

pomIncludeRepository in ThisBuild := { _ => false }

publishTo in ThisBuild := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

SonatypeKeys.profileName in ThisBuild := "knutwalker"
