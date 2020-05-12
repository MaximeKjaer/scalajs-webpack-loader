package io.kjaer.scalajs.webpack

import Dependencies._
import Utils._

class DependenciesSuite extends munit.FunSuite {
  test("parse org.scala-lang.modules::scala-async:0.10.0") {
    val dependency =
      rightOrFail1(parseDependency("org.scala-lang.modules::scala-async:0.10.0")("2.12", "1"))
    assertEquals(dependency.module.organization.value, "org.scala-lang.modules")
    assertEquals(dependency.module.name.value, "scala-async_2.12")
    assertEquals(dependency.version, "0.10.0")
  }

  test("parse org.scalameta:::munit:0.7.2") {
    val dependency = rightOrFail1(parseDependency("org.scalameta:::munit:0.7.2")("2.12", "1"))
    assertEquals(dependency.module.organization.value, "org.scalameta")
    assertEquals(dependency.module.name.value, "munit_sjs1_2.12")
    assertEquals(dependency.version, "0.7.2")
  }

}
