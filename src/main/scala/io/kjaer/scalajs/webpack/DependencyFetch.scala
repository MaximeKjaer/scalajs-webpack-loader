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
      dependencies: Dependencies,
      cacheDirectory: String
  )(implicit logger: WebpackLogger): EitherT[Future, LoaderException, DependencyFiles] =
    for {
      resolution <- EitherT(resolve(dependencies.toSeq))
      files <- EitherT(fetchArtifacts(resolution, cacheDirectory))
    } yield DependencyFiles.fromResolution(resolution, dependencies, files)

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
      implicit logger: WebpackLogger
  ): Future[Either[LoaderException, Map[DependencyName, String]]] = {
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
            implicit val cacheLogger: WebpackCacheLogger = new WebpackCacheLogger(logger)
            val fetchedFile =
              if (fs.existsSync(file)) fetchLocal(artifact, file)
              else download(artifact, file)
            fetchedFile
              .map(either => either.flatMap(name => Right((dependencyName(dependency), name))))
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
      implicit logger: WebpackCacheLogger
  ): Future[Either[String, String]] =
    Future(logger.foundLocally(artifact.url)).map(_ => Right(file))

  private def download(artifact: Artifact, file: String)(
      implicit logger: WebpackCacheLogger
  ): Future[Either[String, String]] =
    Future(logger.downloadingArtifact(artifact.url))
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
        logger.downloadedArtifact(artifact.url, success = true)
        Right(file)
      }
      .recover {
        case e: Exception =>
          logger.downloadedArtifact(artifact.url, success = false)
          val msg = e.toString + Option(e.getMessage).fold("")(" (" + _ + ")")
          Left(msg)
      }
}
