package io.kjaer.scalajs.webpack

import coursier.cache.{Cache, CacheLogger}
import coursier.util.{EitherT, Task}

import scala.concurrent.{ExecutionContext, Future}

class DependencyCache(logger: CacheLogger = CacheLogger.nop) extends Cache[Task] {

  /**
    * Fetch an artifact from a remote repository using an XMLHttpRequest.
    *
    * The implementation of this is based on [[coursier.cache.AlwaysDownload]], but implementing our
    * own Cache gives us control over the XMLHttpRequest and its timeout.
    */
  override def fetch: Cache.Fetch[Task] = { artifact =>
    EitherT(
      Task { implicit ec =>
        Future(logger.downloadingArtifact(artifact.url))
          .flatMap(_ => XMLHttpRequest.get(artifact.url))
          .map { s =>
            logger.downloadedArtifact(artifact.url, success = true)
            Right(s)
          }
          .recover {
            case e: Exception =>
              val msg = e.toString + Option(e.getMessage).fold("")(" (" + _ + ")")
              logger.downloadedArtifact(artifact.url, success = false)
              Left(msg)
          }
      }
    )
  }

  override def ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
}
