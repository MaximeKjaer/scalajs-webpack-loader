package io.kjaer.scalajs.webpack

import coursier.{Dependency, Resolution}

case class DependencyFiles(
    scalaCompiler: DependencyFile,
    scalaJSCompiler: DependencyFile,
    scalaJSLibrary: DependencyFile,
    scalaJSCLI: DependencyFile
)

object DependencyFiles {
  def fromResolution(
      resolution: Resolution,
      dependencies: Dependencies,
      files: Map[DependencyName, String]
  ): DependencyFiles = {
    def file(dependency: Dependency): DependencyFile =
      DependencyFile.fromResolution(dependency, resolution, files)

    DependencyFiles(
      scalaCompiler = file(dependencies.scalaCompiler),
      scalaJSCompiler = file(dependencies.scalaJSCompiler),
      scalaJSLibrary = file(dependencies.scalaJSLib),
      scalaJSCLI = file(dependencies.scalaJSCLI)
    )
  }
}

case class DependencyFile(file: String, transitive: Seq[String]) {
  def classpath: String = (file +: transitive).mkString(":")
}

object DependencyFile {
  def fromResolution(
      dependency: Dependency,
      resolution: Resolution,
      files: Map[DependencyName, String]
  ): DependencyFile = {
    val name = dependencyName(dependency)
    val transitiveDeps = resolution.subset(Seq(dependency)).dependencies.map(dependencyName) - name
    val file = files(name)
    val transitive = files.filter {
      case (dep, _) => transitiveDeps.contains(dep)
    }
    DependencyFile(file, transitive.values.toSeq)
  }
}
