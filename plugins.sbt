import AssemblyKeys._
import scalariform.formatter.preferences._

Revolver.settings

javaOptions in Revolver.reStart += " -Xms4G -Xmx4G"

scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignParameters, true)
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(CompactControlReadability, true)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(PreserveDanglingCloseParenthesis, true)
  .setPreference(RewriteArrowSymbols, true)

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
