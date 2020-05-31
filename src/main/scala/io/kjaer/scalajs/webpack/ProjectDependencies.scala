package io.kjaer.scalajs.webpack

import coursier.{Dependency, Module, ModuleName, Organization}
import coursier.{organizationString => org}
import coursier.{moduleNameString => name}

case class ProjectDependencies(
    scalaCompiler: Dependency,
    scalaJSCompiler: Dependency,
    scalaJSLib: Dependency,
    scalaJSCLI: Dependency,
    libraryDependencies: Seq[Dependency]
) {
  def toSeq: Seq[Dependency] =
    Seq(scalaCompiler, scalaJSCompiler, scalaJSLib, scalaJSCLI) ++ libraryDependencies
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
          scalaJSLib = Dependency(
            Module(org"org.scala-js", ModuleName(s"scalajs-library_${versions.scalaBinVersion}")),
            versions.scalaJSVersion
          ),
          scalaJSCLI = Dependency(
            Module(org"org.scala-js", ModuleName(s"scalajs-cli_${versions.scalaBinVersion}")),
            versions.scalaJSVersion
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
}
