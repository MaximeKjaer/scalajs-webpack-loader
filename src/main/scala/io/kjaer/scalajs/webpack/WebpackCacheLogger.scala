package io.kjaer.scalajs.webpack

import coursier.cache.CacheLogger

class WebpackCacheLogger(logger: WebpackLogger) extends CacheLogger {
  override def downloadingArtifact(url: String): Unit =
    logger.log("Downloading artifact " + url)

  override def downloadedArtifact(url: String, success: Boolean): Unit =
    if (success) logger.log("Downloaded " + url)
    else logger.error("Failed to download " + url)

  override def foundLocally(url: String): Unit =
    logger.log("Found locally " + url)
}
