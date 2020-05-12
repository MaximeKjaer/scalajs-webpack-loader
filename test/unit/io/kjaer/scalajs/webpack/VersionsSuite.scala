package io.kjaer.scalajs.webpack

import Versions._
import Utils._

class VersionsSuite extends munit.FunSuite {
  test("scalaBinaryVersion of 2.12.1 is 2.12") {
    val binVersion = rightOrFail2(scalaBinaryVersion("2.12.1"))
    assertEquals(binVersion, "2.12")
  }

  test("scalaJSBinaryVersion of 1.0.0 is 1") {
    val binVersion = rightOrFail2(scalaJSBinaryVersion("1.0.0"))
    assertEquals(binVersion, "1")
  }

  test("scalaJSBinaryVersion of 1.0.0-RC1 is 1.0-RC1") {
    val binVersion = rightOrFail2(scalaJSBinaryVersion("1.0.0-RC1"))
    assertEquals(binVersion, "1.0-RC1")
  }
}
