import com.typesafe.sbt.SbtScalariform._

import scalariform.formatter.preferences._

object Common {
  lazy val formatterSettings = scalariformSettings ++ List(
    ScalariformKeys.preferences := ScalariformKeys.preferences.value.
      setPreference(AlignParameters, true).
      setPreference(AlignSingleLineCaseStatements, true).
      setPreference(CompactControlReadability, true).
      setPreference(DoubleIndentClassDeclaration, true).
      setPreference(PreserveDanglingCloseParenthesis, true).
      setPreference(RewriteArrowSymbols, true)
  )
}
