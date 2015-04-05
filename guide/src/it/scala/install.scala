/*
 * Copyright 2014 – 2015 Paul Horn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.specs2.Specification

import _root_.buildinfo.BuildInfo
import org.specs2.specification.{Snippets, Forms}

object install extends Specification with Snippets with Forms { lazy val is = "Installation Notes".title ^ s2"""

`$name` is published to Sonatype and can be installed with
your favourite dependency manger:

- [sbt](http://scala-sbt.org)
- [leiningen](http://leiningen.org/)
- [gradle](http://gradle.org)
- [maven](http://maven.apache.org)


#### SBT

```
libraryDependencies += "${me.groupId}" %% "${me.artifactId}" % "${me.version}"
```

#### Leiningen

```
[${me.groupId}/${me.artifactId} "${me.version}"]
```

#### Gradle

```
compile '${me.groupId}:${me.artifactId}:${me.version}'
```

#### Maven

```
<dependency>
  <groupId>${me.groupId}</groupId>
  <artifactId>${me.artifactId}</artifactId>
  <version>${me.version}</version>
</dependency>
```

### Dependencies

$projectDependencies

### Other Modules

$projectModules

"""

  def projectDependencies: String = moduleDependencies(
    name, dependencies,
    s"""`$name` has no additional dependencies besides scala ${BuildInfo.scalaVersion}.""")

  def projectModules: String =
    if (modules.nonEmpty)
      s"""`$name` also comes with the following additional modules:
         |
         |${modules.map(moduleString).mkString("\n")}
      """.stripMargin
    else ""

  def moduleDependencies(name: String, dependencies: List[Dependency], empty: ⇒ String = ""): String =
    if (dependencies.nonEmpty)
      s"""`$name` depends on the following modules:
         |
         |${dependencies.map(_.toString).mkString("- `", "`\n- `", "`\n")}
      """.stripMargin
    else empty

  def moduleString(m: Module): String =
    s"""#### ${m.self.artifactId}
       |
       |`${m.self}`
       |
       |${moduleDependencies(m.self.artifactId, m.deps)}
     """.stripMargin

  val name = BuildInfo.name
  val me = Dependency(name)
  val dependencies = filterDeps(BuildInfo.dependencies)
  val modules = BuildInfo.modules.map(Module(_))

  case class Module(self: Dependency, deps: List[Dependency])
  object Module {
    def apply(nd: (String, Seq[String])): Module =
      Module(Dependency(nd._1), filterDeps(nd._2))
  }

  case class Dependency(groupId: String, artifactId: String, version: String, scope: Option[String]) {
    override def toString: String =
      s""""$groupId" %% "$artifactId" % "$version"${scope.fold("")(s ⇒ " % \"" + s + "\"")}"""
  }
  object Dependency {
    def apply(name: String): Dependency =
      Dependency(BuildInfo.organization, name, BuildInfo.version, None)

    def parse(s: String): Option[Dependency] = {
      val parts = s.split(':')
      if (parts.length == 3) {
        val Array(group, art, version) = parts
        Some(Dependency(group, art, version, None))
      }
      else if (parts.length == 4) {
        val Array(group, art, version, scope) = parts
        Some(Dependency(group, art, version, Some(scope)))
      } else {
        None
      }
    }
  }

  def filterDeps(deps: Seq[String]): List[Dependency] =
    deps.flatMap(Dependency.parse)
      .filterNot(_.scope.exists(_ == "provided"))
      .filterNot(_.artifactId == "scala-library")
      .distinct
      .toList
}
