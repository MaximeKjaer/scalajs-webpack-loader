package io.kjaer.scalajs.webpack

import coursier.{Dependency, MavenRepository, Resolution, Resolve}
import coursier.util.{EitherT, Gather, Task}
import coursier.error.ResolutionError

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DependencyFetch {
  def fetch(
      dependencies: Seq[Dependency],
      cacheDirectory: String
  )(implicit ctx: Context): EitherT[Future, LoaderException, DependencyFiles] =
    for {
      resolution <- EitherT(resolve(dependencies, cacheDirectory))
      files <- EitherT(fetchArtifacts(resolution, cacheDirectory))
    } yield DependencyFiles.fromResolution(resolution, ctx.dependencies, files)

  private def resolve(
      dependencies: Seq[Dependency],
      cacheDirectory: String
  )(implicit ctx: Context): Future[Either[LoaderException, Resolution]] =
    Resolve()
      .withRepositories(Seq(MavenRepository("https://repo1.maven.org/maven2")))
      .withDependencies(dependencies)
      .withCache(new FileContentCache(cacheDirectory))
      .future()
      .map { resolution =>
        if (resolution.errors.nonEmpty)
          Left(ResolutionException(resolution.errors))
        else if (resolution.conflicts.nonEmpty)
          Left(DependencyConflictException(resolution.conflicts))
        else
          Right(resolution)
      }
      .recover {
        case err @ (_: ResolutionError.CantDownloadModule) =>
          Left(DownloadException(Seq(err.getMessage)))
        case err @ (_: ResolutionError.ConflictingDependencies) =>
          Left(DependencyConflictException(err.dependencies))
      }

  private def fetchArtifacts(
      resolutions: Resolution,
      cacheDirectory: String
  )(
      implicit ctx: Context
  ): Future[Either[LoaderException, Map[DependencyId, String]]] = {
    val cache = new FileNameCache(cacheDirectory, Some(ctx.logger))
    Gather[Task]
      .gather(resolutions.dependencyArtifacts().map {
        case (dependency, _, artifact) =>
          cache
            .fetch(artifact)
            .map(fileName => dependencyId(dependency) -> fileName)
            .run
      })
      .future()
      .map { artifacts =>
        val (errors, dependencyFiles) = artifacts.partitionMap(identity)
        if (errors.nonEmpty) Left(DownloadException(errors))
        else Right(dependencyFiles.toMap)
      }
  }
}
