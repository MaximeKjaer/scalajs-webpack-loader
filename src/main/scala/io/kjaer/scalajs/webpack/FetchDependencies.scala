package io.kjaer.scalajs.webpack

import coursier.cache.CacheLogger
import coursier.{Dependency, MavenRepository, Module, Resolve}
import coursier.util.{Artifact, Gather, Task}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object FetchDependencies {
  case class ResolutionException(errors: Seq[((Module, String), Seq[String])])
      extends Exception("Could not get metadata about some dependencies")

  case class DependencyConflictException(conflicts: Set[Dependency])
      extends Exception("Conflicts were found in the dependencies")

  case class FetchException(errors: Seq[String])
      extends Exception("An error happened while fetching artifacts")

  def fetch(
      dependencies: Seq[Dependency]
  )(implicit logger: WebpackLogger): Future[Map[Dependency, String]] =
    resolveDependencies(dependencies)
      .flatMap(fetchArtifacts)
      .map(file => dependencies.zip(file).toMap)

  private def resolveDependencies(dependencies: Seq[Dependency]): Future[Seq[Artifact]] =
    Resolve()
      .withRepositories(Seq(MavenRepository("https://repo1.maven.org/maven2")))
      .withDependencies(dependencies)
      .future()
      .flatMap { resolution =>
        if (resolution.errors.nonEmpty)
          Future.failed(ResolutionException(resolution.errors))
        else if (resolution.conflicts.nonEmpty)
          Future.failed(DependencyConflictException(resolution.conflicts))
        else
          Future.successful(resolution.artifacts())
      }

  private def fetchArtifacts(
      artifacts: Seq[Artifact]
  )(implicit logger: WebpackLogger): Future[Seq[String]] = {
    val cache = new DependencyCache(new WebpackCacheLogger(logger))
    Gather[Task]
      .gather(artifacts.map(cache.fetch(_).run))
      .future()
      .flatMap { artifacts =>
        val (errors, files) = artifacts.partitionMap(identity)
        if (errors.nonEmpty) Future.failed(FetchException(errors))
        else Future.successful(files)
      }
  }
}
