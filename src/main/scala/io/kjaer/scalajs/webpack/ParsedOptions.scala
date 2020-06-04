package io.kjaer.scalajs.webpack

import typings.node.pathMod.{^ => path}

final case class ParsedOptions(
    mainMethod: Option[String],
    moduleKind: String,
    verbosity: String,
    versions: Versions,
    dependencies: ProjectDependencies,
    scalacOptions: Seq[String],
    currentDirectory: String,
    targetDirectory: String
) {
  def targetFile: String = path.join(targetDirectory, "bundle.js")
  def classesDirectory: String = path.join(targetDirectory, "classes")
  // TODO use path.join(os.homedir(), ".ivy2/local")
  def cacheDirectory: String = path.join(currentDirectory, ".cache")
}

object ParsedOptions {
  def parse(options: Options): Either[LoaderException, ParsedOptions] = {
    for {
      versions <- Versions.parse(options.scalaVersion, options.scalaJSVersion)
      dependencies <- ProjectDependencies.parse(options.libraryDependencies.toSeq)(versions)
    } yield {
      val currentDirectory = path.resolve(".")
      val targetDirectory =
        path.join(
          currentDirectory,
          options.targetDirectory,
          s"scala-${versions.scalaBinVersion}"
        )

      ParsedOptions(
        mainMethod = options.mainMethod.toOption,
        moduleKind = options.moduleKind,
        verbosity = options.verbosity,
        versions = versions,
        dependencies = dependencies,
        scalacOptions = options.scalacOptions.toSeq,
        currentDirectory = currentDirectory,
        targetDirectory = targetDirectory
      )
    }
  }
}
