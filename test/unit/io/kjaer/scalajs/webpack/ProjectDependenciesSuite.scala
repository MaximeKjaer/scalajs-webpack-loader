package io.kjaer.scalajs.webpack

import ProjectDependencies._
import Utils._

class ProjectDependenciesSuite extends munit.FunSuite {
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

  test("scalajs-cli version for Scala.js 1.1.0 is 1.0.0") {
    assertEquals(scalaJSCLIVersion("1.1.0"), "1.0.0")
  }

  test("scalajs-cli version for Scala.js 0.6.32 is 0.6.32") {
    assertEquals(scalaJSCLIVersion("0.6.32"), "0.6.32")
  }

  test("scalajs-cli version for Scala.js 0.6.31 is 0.6.31") {
    assertEquals(scalaJSCLIVersion("0.6.31"), "0.6.31")
  }

  test("stringify org.scala-lang.modules::scala-async:0.10.0") {
    val dependency =
      rightOrFail1(parseDependency("org.scala-lang.modules::scala-async:0.10.0")("2.12", "1"))
    assertEquals(stringify(dependency), "org.scala-lang.modules:scala-async_2.12:0.10.0")
  }

  test("stringify org.scalameta:::munit:0.7.2") {
    val dependency =
      rightOrFail1(parseDependency("org.scalameta:::munit:0.7.2")("2.12", "1"))
    assertEquals(stringify(dependency), "org.scalameta:munit_sjs1_2.12:0.7.2")
  }

}
