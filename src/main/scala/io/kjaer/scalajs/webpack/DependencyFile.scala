package io.kjaer.scalajs.webpack

import coursier.{Dependency, Resolution}

case class DependencyFile(jarPath: String, transitiveJarsPaths: Seq[String]) {
  def classpath: Seq[String] = jarPath +: transitiveJarsPaths
}

object DependencyFile {
  def fromResolution(
      resolution: Resolution,
      dependency: Dependency,
      files: Map[DependencyId, String]
  ): DependencyFile = {
    val id = dependencyId(dependency)
    val transitiveDeps = resolution
      .subset(Seq(dependency))
      .dependencies
      .map(dependencyId) - id
    val file = files(id)
    val transitiveFiles = files.filter {
      case (dep, _) => transitiveDeps.contains(dep)
    }
    DependencyFile(file, transitiveFiles.values.toSeq)
  }

  def classpath(dependencyFiles: Seq[DependencyFile]): Seq[String] =
    dependencyFiles.flatMap(_.classpath).distinct
}
