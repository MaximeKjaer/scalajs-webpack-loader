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
}

object Options {
  val schema: JSONSchema7 =
    js.Dynamic
      .literal(
        `type` = js.Array(JSONSchema7TypeName.`null`, JSONSchema7TypeName.`object`),
        additionalProperties = false,
        properties = js.Dynamic.literal(
          mainMethod = js.Dynamic.literal(`type` = JSONSchema7TypeName.string)
        )
      )
      .asInstanceOf[JSONSchema7]

  def get(context: LoaderContext, name: String): Try[Options] = {
    val options = getOptions(context)
    Try {
      validateOptions(schema, options, ValidationErrorConfiguration(name = name))
      options.asInstanceOf[Options]
    }
  }
}
