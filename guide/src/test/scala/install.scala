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

object install extends Specification { def is = s2"""
  ${"Installation Notes".title}

  `$name` is published to Sonatype and can be installed with
  your favourite dependency manger:

  - [sbt](http://scala-sbt.org)
  - [leiningen](http://leiningen.org/)
  - [gradle](http://gradle.org)
  - [maven](http://maven.apache.org)


  <h4>SBT</h4>

  ```
  libraryDependencies += "${me.groupId}" %% "${me.artifactId}" % "${me.version}"
  ```

  <h4>Leiningen</h4>

  ```
  [${me.groupId}/${me.artifactId} "${me.version}"]
  ```

  <h4>Gradle</h4>

  ```
  compile '${me.groupId}:${me.artifactId}:${me.version}'
  ```

  <h4>Maven</h4>

  ```
  <dependency>
    <groupId>${me.groupId}</groupId>
    <artifactId>${me.artifactId}</artifactId>
    <version>${me.version}</version>
  </dependency>
  ```

  <h3>Other Dependencies</h3>

  $projectDependencies

  <h3>Other Modules</h3>

  $projectModules

                                                                             """


  def projectDependencies = {
    if (dependencies.nonEmpty) {
      s"""`$name` depends on the following modules:
        |
        |  ${dependencies.map(_.toString).mkString("- ", "\n  - ", "\n")}
      """.stripMargin
    } else {
      s"""`$name` has no additional dependencies besides scala ${BuildInfo.scalaVersion}."""
    }
  }

  def projectModules = {
    if (modules.nonEmpty) {
      s"""`$name` also comes with the following additional modules:
         |
         |  ${modules.map(moduleString(_, "  ")).mkString("- ", "\n  - ", "\n")}
      """.stripMargin
    } else {
      ""
    }
  }

  def moduleString(m: Module, ident: String) = {
    s"""${m.self}
       |    - Depends on:
       |    ${m.deps.map(_.toString).mkString("- ", s"\n$ident    - ", "\n")}
     """.stripMargin
  }

  def filterDeps(deps: Seq[String]) =
    deps.flatMap(Dependency(_))
      .filterNot(_.scope.contains("provided"))
      .filterNot(_.artifactId == "scala-library")
      .distinct
      .toList

  val name = BuildInfo.name
  val me = Dependency(BuildInfo.organization, name, BuildInfo.version, None)
  val dependencies = filterDeps(BuildInfo.deps_api ++ BuildInfo.deps_core)

  val reactive = Module(
    Dependency(BuildInfo.organization, BuildInfo.name_reactive, BuildInfo.version, None),
    filterDeps(BuildInfo.deps_reactive))
  val rx = Module(
    Dependency(BuildInfo.organization, BuildInfo.name_rx, BuildInfo.version, None),
    filterDeps(BuildInfo.deps_rx))
  val modules = List(reactive, rx)

  case class Module(self: Dependency, deps: List[Dependency])

  case class Dependency(groupId: String, artifactId: String, version: String, scope: Option[String]) {
    override def toString: String =
      s""""$groupId" % "$artifactId" % "$version"${scope.fold("")(s ⇒ s""" % "$s" """)}"""
  }
  object Dependency {
    def apply(s: String): Option[Dependency] = {
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
}
