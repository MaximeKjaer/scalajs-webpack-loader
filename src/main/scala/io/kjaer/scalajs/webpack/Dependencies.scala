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
  def fromOptions(options: Options): Either[Seq[String], Dependencies] = {
    val scalaVersion = options.scalaVersion
    val scalaJSVersion = options.scalaJSVersion
    val scalaBinVersion = binVersion(scalaVersion)
    val scalaJSBinVersion = binVersion(scalaJSVersion)

    val (errors, libraryDependencies) =
      options.libraryDependencies.toSeq
        .map(parse(_, scalaBinVersion, scalaJSBinVersion))
        .partitionMap(identity)

    if (errors.nonEmpty)
      Left(errors)
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

  def binVersion(fullVersion: String): String = fullVersion match {
    case s"$major.0.$_-$milestone"      => s"$major.0-$milestone"
    case s"$major.0.$_"                 => major
    case s"$major.$minor.$_-$milestone" => s"$major.$minor-$milestone"
    case s"$major.$minor.$_"            => s"$major.$minor"
  }

  // TODO use error ADT instead of string
  def parse(
      module: String,
      scalaBinVersion: String,
      scalaJSBinVersion: String
  ): Either[String, Dependency] = module match {
    case s"$org::$name:$version" =>
      Right(
        Dependency(
          Module(
            Organization(org),
            ModuleName(s"${name}_sjs${scalaJSBinVersion}_$scalaBinVersion")
          ),
          version
        )
      )
    case _ =>
      Left(
        s"Could not parse dependency '$module'. Expected a string of the format 'org::name:version'"
      )
  }
}
