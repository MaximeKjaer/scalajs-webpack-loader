package io.kjaer.scalajs.webpack

import scala.scalajs.js
import typings.jsonSchema.mod.JSONSchema7
import typings.jsonSchema.mod.JSONSchema7TypeName
import typings.webpack.mod.loader.LoaderContext
import typings.loaderUtils.mod.getOptions
import typings.schemaUtils.validateMod.ValidationErrorConfiguration
import typings.schemaUtils.{mod => validateOptions}

import scala.util.Try

@js.native
trait Options extends js.Object {
  val mainMethod: js.UndefOr[String]
  val moduleKind: String
  val verbosity: String
  val targetDirectory: String
}

object Options {
  val schema: JSONSchema7 =
    js.Dynamic
      .literal(
        `type` = js.Array(JSONSchema7TypeName.`null`, JSONSchema7TypeName.`object`),
        $schema = "http://json-schema.org/draft-07/schema#",
        description = s"Schema for ${Loader.name} options",
        additionalProperties = false,
        properties = js.Dynamic.literal(
          mainMethod = js.Dynamic.literal(`type` = JSONSchema7TypeName.string),
          moduleKind = js.Dynamic.literal(
            `type` = JSONSchema7TypeName.string,
            enum = js.Array("CommonJSModule", "ESModule", "NoModule")
          ),
          verbosity = js.Dynamic.literal(
            `type` = JSONSchema7TypeName.string,
            enum = js.Array(WebpackLoggerOptions.levels: _*)
          ),
          targetDirectory = js.Dynamic.literal(`type` = JSONSchema7TypeName.string)
        )
      )
      .asInstanceOf[JSONSchema7]

  def defaults: Options =
    js.Dynamic
      .literal(
        mainMethod = js.undefined,
        moduleKind = "CommonJSModule",
        verbosity = "warn",
        targetDirectory = "target"
      )
      .asInstanceOf[Options]

  def get(context: LoaderContext, name: String): Try[Options] = {
    val options = getOptions(context)
    Try {
      validateOptions(schema, options, ValidationErrorConfiguration(name = name))
      js.Object.assign(defaults, options).asInstanceOf[Options]
    }
  }
}
