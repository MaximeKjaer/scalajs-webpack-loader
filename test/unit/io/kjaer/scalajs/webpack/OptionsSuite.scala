package io.kjaer.scalajs.webpack

import typings.schemaUtils.{mod => validateOptions}

class OptionsSuite extends munit.FunSuite {
  test("default options match schema") {
    validateOptions(Options.schema, Options.defaults)
  }

  test("default options are parsed without errors") {
    assert(ParsedOptions.parse(Options.defaults).isRight)
  }
}
