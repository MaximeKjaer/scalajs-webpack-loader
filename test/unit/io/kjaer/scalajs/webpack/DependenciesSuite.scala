package io.kjaer.scalajs.webpack

import Dependencies._

class DependenciesSuite extends munit.FunSuite {
  test("scalaBinaryVersion of 2.12.1 is 2.12") {
    assertEquals(scalaBinaryVersion("2.12.1"), "2.12")
  }

  test("scalaJSBinaryVersion of 1.0.0 is 1") {
    assertEquals(scalaJSBinaryVersion("1.0.0"), "1")
  }

  test("scalaJSBinaryVersion of 1.0.0-RC1 is 1.0-RC1") {
    assertEquals(scalaJSBinaryVersion("1.0.0-RC1"), "1.0-RC1")
  }

  @inline def rightOrFail[T](either: Either[String, T]): T = either match {
    case Right(value)  => value
    case Left(message) => fail(message)
  }

  test("parse org.scala-lang.modules::scala-async:0.10.0") {
    val dependency = rightOrFail(parse("org.scala-lang.modules::scala-async:0.10.0", "2.12", "1"))
    assertEquals(dependency.module.organization.value, "org.scala-lang.modules")
    assertEquals(dependency.module.name.value, "scala-async_2.12")
    assertEquals(dependency.version, "0.10.0")
  }

  test("parse org.scalameta:::munit:0.7.2") {
    val dependency = rightOrFail(parse("org.scalameta:::munit:0.7.2", "2.12", "1"))
    assertEquals(dependency.module.organization.value, "org.scalameta")
    assertEquals(dependency.module.name.value, "munit_sjs1_2.12")
    assertEquals(dependency.version, "0.7.2")
  }

}
