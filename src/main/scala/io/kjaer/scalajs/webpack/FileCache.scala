package io.kjaer.scalajs.webpack

import coursier.cache.{Cache, CacheLogger}
import coursier.cache.Cache.Fetch
import coursier.util.{EitherT, Task}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom.experimental.URL
import typings.node.pathMod.{^ => path}
import typings.fsExtra.{mod => fs}
import typings.node.nodeStrings
import typings.nodeFetch.mod.{default => jsFetch}

import scala.scalajs.js

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
  protected def download(url: String, filePath: String): Future[Either[String, String]] =
    Future(loggerOpt.foreach(_.downloadingArtifact(url)))
      .flatMap { _ =>
        fs.ensureDirSync(path.dirname(filePath))
        val writeStream = fs.createWriteStream(filePath)
        jsFetch(url).toFuture.foreach(_.body.pipe(writeStream))
        val promise = Promise[String]()
        writeStream.on_finish(nodeStrings.finish, () => promise.success(filePath))
        writeStream.on_error(nodeStrings.error, err => promise.failure(js.JavaScriptException(err)))
        promise.future
      }
      .map { filePath =>
        loggerOpt.foreach(_.downloadedArtifact(url, success = true))
        Right(filePath)
      }
      .recover {
        case e: Exception =>
          loggerOpt.foreach(_.downloadedArtifact(url, success = false))
          val msg = e.toString + Option(e.getMessage).fold("")(" (" + _ + ")")
          Left(msg)
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
  ): Future[Either[String, String]] =
    for {
      _ <- download(url, filePath)
      content <- fetchLocal(filePath)
    } yield content
}
