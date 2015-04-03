resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.eed3si9n"       % "sbt-assembly"          % "0.13.0")
addSbtPlugin("com.eed3si9n"       % "sbt-buildinfo"         % "0.4.0")
addSbtPlugin("de.heikoseeberger"  % "sbt-header"            % "1.4.0")
addSbtPlugin("pl.project13.scala" % "sbt-jmh"               % "0.1.12")
addSbtPlugin("com.typesafe.sbt"   % "sbt-pgp"               % "0.8.3")
addSbtPlugin("com.github.gseitz"  % "sbt-release"           % "0.8.5")
addSbtPlugin("org.scalastyle"    %% "scalastyle-sbt-plugin" % "0.6.0")
addSbtPlugin("org.scoverage"      % "sbt-scoverage"         % "1.0.4")
addSbtPlugin("org.xerial.sbt"     % "sbt-sonatype"          % "0.2.2")
