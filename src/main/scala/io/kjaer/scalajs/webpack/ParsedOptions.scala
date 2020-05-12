package io.kjaer.scalajs.webpack

final case class ParsedOptions(
    mainMethod: Option[String],
    moduleKind: String,
    verbosity: String,
    targetDirectory: String,
    versions: Versions,
    dependencies: Dependencies,
    scalacOptions: Seq[String]
)

object ParsedOptions {
  def parse(options: Options): Either[LoaderException, ParsedOptions] =
    for {
      versions <- Versions.parse(options.scalaVersion, options.scalaJSVersion)
      dependencies <- Dependencies.parse(options.libraryDependencies.toSeq)(versions)
    } yield ParsedOptions(
      mainMethod = options.mainMethod.toOption,
      moduleKind = options.moduleKind,
      verbosity = options.verbosity,
      targetDirectory = options.targetDirectory,
      versions = versions,
      dependencies = dependencies,
      scalacOptions = options.scalacOptions.toSeq
    )
}
