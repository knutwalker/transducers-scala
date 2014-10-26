libraryDependencies ++= {
  object Version {
    val scalatest   = "2.2.2"
    val scalacheck  = "1.11.6"
  }
  object Library {
    val scalatest   = "org.scalatest"  %% "scalatest"  % Version.scalatest
    val scalacheck  = "org.scalacheck" %% "scalacheck" % Version.scalacheck
  }
  List(
    Library.scalatest  % "test",
    Library.scalacheck % "test"
  )
}
