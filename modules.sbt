import JmhKeys._

lazy val root = project.in(file("."))

lazy val benchmark = project
  .dependsOn(root)
  .settings(jmhSettings: _*)
  .settings(outputTarget in Jmh := target.value / s"scala-${scalaBinaryVersion.value}")
  .settings(libraryDependencies ++= List(
    "com.cognitect" % "transducers-java" % "0.4.67"))
