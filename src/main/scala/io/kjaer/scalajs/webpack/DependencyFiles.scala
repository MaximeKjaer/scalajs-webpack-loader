package io.kjaer.scalajs.webpack

import coursier.{Dependency, Resolution}

case class DependencyFiles(
    scalaCompiler: Map[DependencyName, String],
    scalaJSCompiler: String,
    scalaJSLibrary: Map[DependencyName, String]
)

object DependencyFiles {
  def fromResolution(
      resolution: Resolution,
      dependencies: Dependencies,
      files: Map[DependencyName, String]
  ): DependencyFiles = {
    def transitiveDependencies(dependency: Dependency): Map[DependencyName, String] = {
      val transitiveDeps = resolution.subset(Seq(dependency)).dependencies.map(dependencyName)
      files.filter {
        case (dep, _) => transitiveDeps.contains(dep)
      }
    }

    DependencyFiles(
      scalaCompiler = transitiveDependencies(dependencies.scalaCompiler),
      scalaJSCompiler = files(dependencyName(dependencies.scalaJSCompiler)),
      scalaJSLibrary = transitiveDependencies(dependencies.scalaJSLib)
    )
  }
}
