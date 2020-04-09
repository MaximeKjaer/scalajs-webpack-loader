package io.kjaer.scalajs.webpack

import coursier.{Dependency, Resolution}

case class DependencyFiles(
    scalaCompiler: DependencyFile,
    scalaJSCompiler: DependencyFile,
    scalaJSLibrary: DependencyFile,
    scalaJSCLI: DependencyFile,
    libraryDependencies: Seq[DependencyFile]
)

case class DependencyFile(file: String, transitiveFiles: Seq[String]) {
  def allFiles: Seq[String] = file +: transitiveFiles
}

object DependencyFiles {
  def fromResolution(
      resolution: Resolution,
      dependencies: Dependencies,
      files: Map[DependencyId, String]
  ): DependencyFiles = {
    def dependencyFile(dependency: Dependency): DependencyFile = {
      val name = dependencyId(dependency)
      val transitiveDeps = resolution
        .subset(Seq(dependency))
        .dependencies
        .map(dependencyId) - name
      val file = files(name)
      val transitive = files.filter {
        case (dep, _) => transitiveDeps.contains(dep)
      }
      DependencyFile(file, transitive.values.toSeq)
    }

    DependencyFiles(
      scalaCompiler = dependencyFile(dependencies.scalaCompiler),
      scalaJSCompiler = dependencyFile(dependencies.scalaJSCompiler),
      scalaJSLibrary = dependencyFile(dependencies.scalaJSLib),
      scalaJSCLI = dependencyFile(dependencies.scalaJSCLI),
      libraryDependencies = dependencies.libraryDependencies.map(dependencyFile)
    )
  }

  def classpath(dependencyFile: DependencyFile): String =
    dependencyFile.allFiles.mkString(":")

  def classpath(dependencyFiles: Seq[DependencyFile]): String =
    dependencyFiles.flatMap(_.allFiles).toSet.mkString(":")
}
