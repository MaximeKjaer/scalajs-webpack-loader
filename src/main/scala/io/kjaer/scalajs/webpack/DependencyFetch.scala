package io.kjaer.scalajs.webpack

import coursier.{Dependency, MavenRepository, Module, Resolve}
import coursier.util.{Artifact, Gather, Task}

import typings.fsExtra.{mod => fs}
import typings.node.nodeStrings
import typings.node.pathMod.{^ => path}
import typings.nodeFetch.mod.{default => fetch}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js

object DependencyFetch {
  case class ResolutionException(errors: Seq[((Module, String), Seq[String])])
      extends Exception("Could not get metadata about some dependencies")

  case class DependencyConflictException(conflicts: Set[Dependency])
      extends Exception("Conflicts were found in the dependencies")

  case class FetchException(errors: Seq[String])
      extends Exception("An error happened while fetching artifacts")

  def fetchDependencies(
      dependencies: Seq[Dependency],
      cacheDirectory: String
  )(implicit logger: WebpackLogger): Future[Map[Dependency, String]] =
    resolve(dependencies)
      .flatMap(fetchArtifacts(_, cacheDirectory))
      .map(_.toMap)

  private def resolve(dependencies: Seq[Dependency]): Future[Seq[(Dependency, Artifact)]] =
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
          Future.successful(dependencies.zip(resolution.artifacts()))
      }

  private def fetchArtifacts(
      resolutions: Seq[(Dependency, Artifact)],
      cacheDirectory: String
  )(implicit logger: WebpackLogger): Future[Seq[(Dependency, String)]] = {
    Gather[Task]
      .gather(resolutions.map {
        case (dependency, artifact) =>
          val file = path.join(
            cacheDirectory,
            dependency.module.organization.value,
            dependency.version,
            "jars",
            s"${dependency.module.name.value}.jar"
          )
          download(artifact, file)(new WebpackCacheLogger(logger)) // TODO implicit
      })
      .future()
      .flatMap { artifacts =>
        val (errors, files) = artifacts.partitionMap(identity)
        if (errors.nonEmpty) Future.failed(FetchException(errors))
        else Future.successful(resolutions.map(_._1).zip(files)) // TODO don't unzip/rezip
      }
  }

  private def download(artifact: Artifact, file: String)(
      implicit logger: WebpackCacheLogger
  ): Task[Either[String, String]] = Task { _ =>
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
}
