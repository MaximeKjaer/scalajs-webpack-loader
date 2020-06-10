package io.kjaer.scalajs.webpack

import coursier.{Dependency, Module, ModuleName, Organization}
import coursier.{organizationString => org}
import coursier.{moduleNameString => name}

case class ProjectDependencies(
    scalaCompiler: Dependency,
    scalaJSCompiler: Dependency,
    scalaJSLinker: Dependency,
    scalaJSLib: Dependency,
    scalaJSCLI: Dependency,
    libraryDependencies: Seq[Dependency]
) {
  def toSeq: Seq[Dependency] =
    Seq(scalaCompiler, scalaJSCompiler, scalaJSLib, scalaJSCLI, scalaJSLinker) ++ libraryDependencies
}

object ProjectDependencies {
  def parse(
      libraryDependencies: Seq[String]
  )(versions: Versions): Either[LoaderException, ProjectDependencies] = {
    val (errors, parsedDependencies) =
      libraryDependencies
        .map(parseDependency(_)(versions.scalaBinVersion, versions.scalaJSBinVersion))
        .partitionMap(identity)

    if (errors.nonEmpty)
      Left(LibraryDependenciesParseException(errors))
    else
      Right(
        ProjectDependencies(
          scalaCompiler =
            Dependency(Module(org"org.scala-lang", name"scala-compiler"), versions.scalaVersion),
          scalaJSCompiler = Dependency(
            Module(org"org.scala-js", ModuleName(s"scalajs-compiler_${versions.scalaVersion}")),
            versions.scalaJSVersion
          ),
          scalaJSLinker = Dependency(
            Module(org"org.scala-js", ModuleName(s"scalajs-linker_${versions.scalaBinVersion}")),
            versions.scalaJSVersion
          ),
          scalaJSLib = Dependency(
            Module(org"org.scala-js", ModuleName(s"scalajs-library_${versions.scalaBinVersion}")),
            versions.scalaJSVersion
          ),
          scalaJSCLI = Dependency(
            Module(org"org.scala-js", ModuleName(s"scalajs-cli_${versions.scalaBinVersion}")),
            scalaJSCLIVersion(versions.scalaJSVersion)
          ),
          libraryDependencies = parsedDependencies
        )
      )
  }

  private[webpack] def parseDependency(dependency: String)(
      scalaBinVersion: String,
      scalaJSBinVersion: String
  ): Either[String, Dependency] = dependency match {
    case s"$org:::$name:$version" =>
      Right(
        Dependency(
          Module(
            Organization(org),
            ModuleName(s"${name}_sjs${scalaJSBinVersion}_$scalaBinVersion")
          ),
          version
        )
      )
    case s"$org::$name:$version" =>
      Right(Dependency(Module(Organization(org), ModuleName(s"${name}_$scalaBinVersion")), version))
    case _ =>
      Left(
        s"Could not parse dependency '$dependency'. Expected a string of the format " +
          "'org::name:version' or 'org:::name:version'"
      )
  }

  /**
    * Returns the version of the Scala.js CLI to use to link with the given Scala.js version.
    *
    * For Scala.js 1.x, the CLI should be version 1.0.0; the version of the linker is set not by
    * using a specific CLI version, but by loading the correct linker onto the classpath of the CLI.
    *
    * For Scala.js 0.6.x, however, the CLI should have the same version number as the Scala.js
    * linker.
    *
    * @param scalaJSVersion Scala.js version with which the project should be compiled
    * @return Version of `org.scala-js:scalajs-cli` that should be used
    */
  private[webpack] def scalaJSCLIVersion(scalaJSVersion: String): String =
    if (scalaJSVersion.startsWith("1")) "1.0.0"
    else scalaJSVersion
}
