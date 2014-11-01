import AssemblyKeys._

assemblySettings

jarName in assembly := s"${name.value}-${version.value}"

outputPath in assembly := baseDirectory.value / ".." / (jarName in assembly).value

assemblyOption in assembly ~= { _.copy(prependShellScript = Some(defaultShellScript)) }

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
  case x @ PathList("META-INF", xs @ _*) =>
    (xs map (_.toLowerCase)) match {
      case ("changes.txt" :: Nil) | ("licenses.txt" :: Nil) => MergeStrategy.rename
      case _ => old(x)
    }
  case "CHANGES.txt" | "LICENSE.txt"   => MergeStrategy.rename
  case x   => old(x)
}}
