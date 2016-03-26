resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.eed3si9n"       % "sbt-assembly"          % "0.14.1")
addSbtPlugin("com.eed3si9n"       % "sbt-buildinfo"         % "0.6.1")
addSbtPlugin("de.heikoseeberger"  % "sbt-header"            % "1.5.1")
addSbtPlugin("pl.project13.scala" % "sbt-jmh"               % "0.2.6")
addSbtPlugin("com.jsuereth"       % "sbt-pgp"               % "1.0.0")
addSbtPlugin("com.github.gseitz"  % "sbt-release"           % "1.0.3")
addSbtPlugin("org.scalastyle"    %% "scalastyle-sbt-plugin" % "0.8.0")
addSbtPlugin("org.scoverage"      % "sbt-scoverage"         % "1.3.5")
addSbtPlugin("org.xerial.sbt"     % "sbt-sonatype"          % "1.1")
