package io.kjaer.scalajs.webpack

import coursier.{Dependency, MavenRepository, Resolution, Resolve}
import coursier.util.{Artifact, EitherT, Gather, Task}
import typings.fsExtra.{mod => fs}
import typings.node.nodeStrings
import typings.node.pathMod.{^ => path}
import typings.nodeFetch.mod.{default => fetch}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js

object DependencyFetch {
  def fetchDependencies(
      cacheDirectory: String
  )(implicit ctx: Context): EitherT[Future, LoaderException, DependencyFiles] =
    for {
      resolution <- EitherT(resolve(ctx.dependencies.toSeq))
      files <- EitherT(fetchArtifacts(resolution, cacheDirectory))
    } yield DependencyFiles.fromResolution(resolution, ctx.dependencies, files)

  private def resolve(dependencies: Seq[Dependency]): Future[Either[LoaderException, Resolution]] =
    Resolve()
      .withRepositories(Seq(MavenRepository("https://repo1.maven.org/maven2")))
      .withDependencies(dependencies)
      .future()
      .map { resolution =>
        if (resolution.errors.nonEmpty)
          Left(ResolutionException(resolution.errors))
        else if (resolution.conflicts.nonEmpty)
          Left(DependencyConflictException(resolution.conflicts))
        else
          Right(resolution)
      }

  private def fetchArtifacts(
      resolutions: Resolution,
      cacheDirectory: String
  )(
      implicit ctx: Context
  ): Future[Either[LoaderException, Map[DependencyId, String]]] = {
    Gather[Task]
      .gather(resolutions.dependencyArtifacts().map {
        case (dependency, _, artifact) =>
          val file = path.join(
            cacheDirectory,
            dependency.module.organization.value,
            dependency.version,
            "jars",
            s"${dependency.module.name.value}.jar"
          )
          Task { _ =>
            val fetchedFile =
              if (fs.existsSync(file)) fetchLocal(artifact, file)
              else download(artifact, file)
            fetchedFile
              .map(either => either.flatMap(name => Right((dependencyId(dependency), name))))
          }
      })
      .future()
      .map { artifacts =>
        val (errors, dependencyFiles) = artifacts.partitionMap(identity)
        if (errors.nonEmpty) Left(DownloadException(errors))
        else Right(dependencyFiles.toMap)
      }
  }

  private def fetchLocal(artifact: Artifact, file: String)(
      implicit ctx: Context
  ): Future[Either[String, String]] =
    Future(ctx.logger.foundLocally(artifact.url)).map(_ => Right(file))

  private def download(artifact: Artifact, file: String)(
      implicit ctx: Context
  ): Future[Either[String, String]] =
    Future(ctx.logger.downloadingArtifact(artifact.url))
      .flatMap { _ =>
        fs.ensureDirSync(path.dirname(file))
        val writeStream = fs.createWriteStream(file)
        fetch(artifact.url).toFuture.foreach(_.body.pipe(writeStream))
        val promise = Promise[String]()
        writeStream.on_finish(nodeStrings.finish, () => promise.success(file))
        writeStream.on_error(nodeStrings.error, err => promise.failure(js.JavaScriptException(err)))
        promise.future
      }
      .map { file =>
        ctx.logger.downloadedArtifact(artifact.url, success = true)
        Right(file)
      }
      .recover {
        case e: Exception =>
          ctx.logger.downloadedArtifact(artifact.url, success = false)
          val msg = e.toString + Option(e.getMessage).fold("")(" (" + _ + ")")
          Left(msg)
      }
}
