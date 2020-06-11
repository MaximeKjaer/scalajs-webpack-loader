package io.kjaer.scalajs.webpack

import munit.Assertions.fail

object Utils {
  def getRight[L, R](either: Either[L, R])(implicit loc: munit.Location): R = either match {
    case Right(value)          => value
    case Left(err: Throwable)  => fail("Could not get right", err)
    case Left(message: String) => fail(message)
    case Left(left)            => fail(s"Could not get right, got ${left.toString}")
  }
}
