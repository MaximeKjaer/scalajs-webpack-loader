package io.kjaer.scalajs.webpack

class DependenciesSuite extends munit.FunSuite {
  test("binVersion of 2.12.1 is 2.12") {
    assertEquals(Dependencies.binVersion("2.12.1"), "2.12")
  }

  test("binVersion of 1.0.0 is 1") {
    assertEquals(Dependencies.binVersion("1.0.0"), "1")
  }

  test("binVersion of 1.0.0-RC1 is 1.0-RC1") {
    assertEquals(Dependencies.binVersion("1.0.0-RC1"), "1.0-RC1")
  }

  test("parses org::name:version successfully") {
    val dependency = Dependencies.parse("org::name:version", "2.12", "1") match {
      case Right(parsed) => parsed
      case Left(message) => fail(message)
    }

    assertEquals(dependency.module.organization.value, "org")
    assertEquals(dependency.module.name.value, "name_sjs1_2.12")
    assertEquals(dependency.version, "version")
  }

}
