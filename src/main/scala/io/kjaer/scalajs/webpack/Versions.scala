package io.kjaer.scalajs.webpack

final case class Versions(
    scalaVersion: String,
    scalaBinVersion: String,
    scalaJSVersion: String,
    scalaJSBinVersion: String
)

object Versions {
  def parse(
      scalaVersion: String,
      scalaJSVersion: String
  ): Either[LoaderException, Versions] =
    for {
      scalaBinVersion <- scalaBinaryVersion(scalaVersion)
      scalaJSBinVersion <- scalaJSBinaryVersion(scalaJSVersion)
    } yield Versions(scalaVersion, scalaBinVersion, scalaJSVersion, scalaJSBinVersion)

  private val ReleaseVersion = """(\d+)\.(\d+)\.(\d+)""".r
  private val MinorSnapshotVersion = """(\d+)\.(\d+)\.([1-9]\d*)-SNAPSHOT""".r

  private[webpack] def scalaBinaryVersion(scalaVersion: String): Either[LoaderException, String] =
    scalaVersion match {
      case ReleaseVersion(major, minor, _)       => Right(s"$major.$minor")
      case MinorSnapshotVersion(major, minor, _) => Right(s"$major.$minor")
      case _                                     => Left(ScalaVersionParseException(scalaVersion))
    }

  private val ScalaJSFullVersion = """^(\d+)\.(\d+)\.(\d+)(-.*)?$""".r

  private[webpack] def scalaJSBinaryVersion(
      scalaJSVersion: String
  ): Either[LoaderException, String] =
    scalaJSVersion match {
      case ScalaJSFullVersion("0", "6", _, _) => Right("0.6")
      case ScalaJSFullVersion(major, "0", "0", suffix) if suffix != null =>
        Right(s"$major.0$suffix")
      case ScalaJSFullVersion(major, _, _, _) => Right(major)
      case _                                  => Left(ScalaJSVersionParseException(scalaJSVersion))
    }
}
