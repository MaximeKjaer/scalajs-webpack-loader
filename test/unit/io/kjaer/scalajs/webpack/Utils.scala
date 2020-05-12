package io.kjaer.scalajs.webpack

object Utils {
  def rightOrFail1[T](either: Either[String, T]): T = either match {
    case Right(value)  => value
    case Left(message) => throw new RuntimeException(message)
  }

  def rightOrFail2[T](either: Either[LoaderException, T]): T = either match {
    case Right(value) => value
    case Left(err)    => throw err
  }
}
