package io.kjaer.scalajs.webpack

import coursier._

case class Dependencies(
    scalaBinVersion: String,
    scalaCompiler: Dependency,
    scalaJSCompiler: Dependency,
    scalaJSLib: Dependency,
    scalaJSCLI: Dependency,
    libraryDependencies: Seq[Dependency]
) {

  def toSeq: Seq[Dependency] =
    Seq(scalaCompiler, scalaJSCompiler, scalaJSLib, scalaJSCLI) ++ libraryDependencies
}

object Dependencies {
  def fromOptions(options: Options): Either[LoaderException, Dependencies] = {
    val scalaVersion = options.scalaVersion
    val scalaJSVersion = options.scalaJSVersion
    val scalaBinVersion = scalaBinaryVersion(scalaVersion)
    val scalaJSBinVersion = scalaJSBinaryVersion(scalaJSVersion)

    val (errors, libraryDependencies) =
      options.libraryDependencies.toSeq
        .map(parse(_, scalaBinVersion, scalaJSBinVersion))
        .partitionMap(identity)

    if (errors.nonEmpty)
      Left(LibraryDependenciesParseException(errors))
    else
      Right(
        Dependencies(
          scalaBinVersion = scalaBinVersion,
          scalaCompiler =
            Dependency(Module(org"org.scala-lang", name"scala-compiler"), scalaVersion),
          scalaJSCompiler = Dependency(
            Module(org"org.scala-js", ModuleName(s"scalajs-compiler_$scalaVersion")),
            scalaJSVersion
          ),
          scalaJSLib = Dependency(
            Module(org"org.scala-js", ModuleName(s"scalajs-library_$scalaBinVersion")),
            scalaJSVersion
          ),
          scalaJSCLI = Dependency(
            Module(org"org.scala-js", ModuleName(s"scalajs-cli_$scalaBinVersion")),
            scalaJSVersion
          ),
          libraryDependencies = libraryDependencies
        )
      )
  }

  private val ScalaJSFullVersion = """^(\d+)\.(\d+)\.(\d+)(-.*)?$""".r

  def scalaJSBinaryVersion(scalaJSVersion: String): String = scalaJSVersion match {
    case ScalaJSFullVersion("0", "6", _, _)                            => "0.6"
    case ScalaJSFullVersion(major, "0", "0", suffix) if suffix != null => s"$major.0$suffix"
    case ScalaJSFullVersion(major, _, _, _)                            => major
  }

  private val ReleaseVersion = """(\d+)\.(\d+)\.(\d+)""".r
  private val MinorSnapshotVersion = """(\d+)\.(\d+)\.([1-9]\d*)-SNAPSHOT""".r

  def scalaBinaryVersion(scalaVersion: String): String = scalaVersion match {
    case ReleaseVersion(major, minor, _)       => s"$major.$minor"
    case MinorSnapshotVersion(major, minor, _) => s"$major.$minor"
    case _                                     => scalaVersion
  }

  def parse(
      module: String,
      scalaBinVersion: String,
      scalaJSBinVersion: String
  ): Either[String, Dependency] = module match {
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
        s"Could not parse dependency '$module'. Expected a string of the format " +
          "'org::name:version' or 'org:::name:version'"
      )
  }
}
