package io.kjaer.scalajs.webpack

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

import org.scalajs.dom.experimental.URL

import coursier.cache.{Cache, CacheLogger}
import coursier.cache.Cache.Fetch
import coursier.util.{EitherT, Task}

import typings.node.pathMod.{^ => path}
import typings.fsExtra.{mod => fs}
import typings.node.nodeStrings
import typings.nodeFetch.mod.{default => jsFetch}
import typings.node.NodeJS.ReadableStream

sealed abstract class FileCache(
    cacheDir: String,
    private val _loggerOpt: Option[CacheLogger] = None
) extends Cache[Task] {

  override def loggerOpt: Option[CacheLogger] = _loggerOpt

  override def fetch: Fetch[Task] =
    artifact =>
      EitherT(
        Task { _ =>
          val url = new URL(artifact.url)
          val artifactPath = path.join(
            cacheDir,
            url.protocol.dropRight(1), // Drop final ":"
            url.hostname,
            url.pathname.split("/").mkString(path.sep)
          )

          for {
            pathExists <- fs.pathExists(artifactPath).toFuture
            file <- (if (pathExists) fetchLocal(artifactPath)
                     else fetchRemote(artifact.url, artifactPath))
          } yield file
        }
      )

  /**
    * Fetch a file from the local cache. Assumes that the file exists in the cache.
    * @param filePath Path to the file
    * @return A Future containing either an error message, or a string for the file.
    */
  protected def fetchLocal(filePath: String): Future[Either[String, String]]

  /**
    * Fetch a file from a remote URL.
    * @param url HTTP URL to the file
    * @param filePath Destination file for the download
    * @return A Future containing either an error message, or a string for the file.
    */
  protected def fetchRemote(url: String, filePath: String): Future[Either[String, String]]

  /**
    * Download a file from a remote URL to a local file.
    * @param url HTTP URL to the file
    * @param filePath Destination file for the download
    * @return A Future containing either an error message, or the local file path for the remote file.
    */
  protected def download(url: String, filePath: String): Future[Either[String, String]] = {
    def streamToFile(inputStream: ReadableStream): Future[Unit] = {
      val writeStream = fs.createWriteStream(filePath)
      val promise = Promise[Unit]()
      writeStream.on_finish(nodeStrings.finish, () => promise.success(filePath))
      writeStream.on_error(
        nodeStrings.error,
        err => promise.failure(js.JavaScriptException(err))
      )
      inputStream.pipe(writeStream)
      val future = promise.future
      future.onComplete(_ => writeStream.close())
      future
    }

    Future(loggerOpt.foreach(_.downloadingArtifact(url)))
      .flatMap(_ => jsFetch(url).toFuture)
      .map { response =>
        if (response.ok) Right(response.body)
        else Left(response.status)
      }
      .flatMap {
        case Left(statusCode) =>
          Future.successful(Left(s"Server replied with HTTP $statusCode"))
        case Right(bodyStream) =>
          fs.ensureDirSync(path.dirname(filePath))
          streamToFile(bodyStream)
            .map(_ => loggerOpt.foreach(logger => logger.downloadedArtifact(url, success = true)))
            .map(_ => Right(filePath))
      }
  }

  override def ec: ExecutionContext = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
}

/** Fetches filenames from the cache. Downloads artifacts if needed. */
class FileNameCache(cacheDir: String, _loggerOpt: Option[CacheLogger] = None)
    extends FileCache(cacheDir, _loggerOpt) {

  override protected def fetchLocal(filePath: String): Future[Either[String, String]] =
    Future.successful(Right(filePath))

  override protected def fetchRemote(
      url: String,
      filePath: String
  ): Future[Either[String, String]] =
    download(url, filePath)
}

/** Fetches file contents from the cache. Downloads artifacts if needed. */
class FileContentCache(cacheDir: String, _loggerOpt: Option[CacheLogger] = None)
    extends FileCache(cacheDir, _loggerOpt) {
  override protected def fetchLocal(filePath: String): Future[Either[String, String]] =
    fs.readFile(filePath)
      .toFuture
      .map(_.toString("utf-8"))
      .map(Right(_))
      .recover(err => Left(err.getMessage))

  override protected def fetchRemote(
      url: String,
      filePath: String
  ): Future[Either[String, String]] = {
    val futureContent = for {
      _ <- EitherT(download(url, filePath))
      content <- EitherT(fetchLocal(filePath))
    } yield content
    futureContent.run
  }
}
