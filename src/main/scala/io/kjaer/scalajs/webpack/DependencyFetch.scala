package io.kjaer.scalajs.webpack

import coursier.{Dependency, MavenRepository, Module, Resolve, Resolution}
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

  private def resolve(
      dependencies: Seq[Dependency]
  ): Future[Resolution] =
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
          Future.successful(resolution)
      }

  private def fetchArtifacts(
      resolutions: Resolution,
      cacheDirectory: String
  )(implicit logger: WebpackLogger): Future[Seq[(Dependency, String)]] = {
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
          Task(_ => download(dependency, artifact, file)(new WebpackCacheLogger(logger)))
      })
      .future()
      .flatMap { artifacts =>
        val (errors, dependencyFiles) = artifacts.partitionMap(identity)
        if (errors.nonEmpty) Future.failed(FetchException(errors))
        else Future.successful(dependencyFiles)
      }
  }

  private def download(dependency: Dependency, artifact: Artifact, file: String)(
      implicit logger: WebpackCacheLogger
  ): Future[Either[String, (Dependency, String)]] =
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
        Right((dependency, file))
      }
      .recover {
        case e: Exception =>
          logger.downloadedArtifact(artifact.url, success = false)
          val msg = e.toString + Option(e.getMessage).fold("")(" (" + _ + ")")
          Left(msg)
      }
}