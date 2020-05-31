package io.kjaer.scalajs.webpack

import coursier.{Dependency, Resolution}

case class ProjectDependencyFiles(
    scalaCompiler: DependencyFile,
    scalaJSCompiler: DependencyFile,
    scalaJSLibrary: DependencyFile,
    scalaJSCLI: DependencyFile,
    libraryDependencies: Seq[DependencyFile]
) {

  /** Classpath needed to compile the project */
  def classpath: Seq[String] = DependencyFile.classpath(scalaJSLibrary +: libraryDependencies)
}

object ProjectDependencyFiles {
  def fromResolution(
      resolution: Resolution,
      dependencies: ProjectDependencies,
      files: Map[DependencyId, String]
  ): ProjectDependencyFiles = {
    def dependencyFile(dependency: Dependency): DependencyFile =
      DependencyFile.fromResolution(resolution, dependency, files)

    ProjectDependencyFiles(
      scalaCompiler = dependencyFile(dependencies.scalaCompiler),
      scalaJSCompiler = dependencyFile(dependencies.scalaJSCompiler),
      scalaJSLibrary = dependencyFile(dependencies.scalaJSLib),
      scalaJSCLI = dependencyFile(dependencies.scalaJSCLI),
      libraryDependencies = dependencies.libraryDependencies.map(dependencyFile)
    )
  }
}
