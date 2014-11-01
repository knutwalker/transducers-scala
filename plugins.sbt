import AssemblyKeys._

assemblySettings

jarName in assembly := s"${name.value}_${scalaBinaryVersion.value}-${version.value}.jar"

outputPath in assembly := baseDirectory.value / (jarName in assembly).value

assemblyOption in assembly ~= { o =>
  o.copy(prependShellScript = Some(defaultShellScript))
   .copy(includeScala = false) }

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
  case x @ PathList("META-INF", xs @ _*) =>
    (xs map (_.toLowerCase)) match {
      case ("changes.txt" :: Nil) | ("licenses.txt" :: Nil) => MergeStrategy.rename
      case _ => old(x)
    }
  case "CHANGES.txt" | "LICENSE.txt"   => MergeStrategy.rename
  case x   => old(x)
}}
