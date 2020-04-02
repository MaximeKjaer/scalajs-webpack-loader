package io.kjaer.scalajs.webpack

import scala.scalajs.js
import typings.jsonSchema.mod.JSONSchema7
import typings.jsonSchema.mod.JSONSchema7TypeName
import typings.webpack.mod.loader.LoaderContext
import typings.loaderUtils.mod.getOptions
import typings.schemaUtils.validateMod.ValidationErrorConfiguration
import typings.schemaUtils.{mod => validateOptions}

import scala.util.Try

trait Options extends js.Object {
  val mainMethod: js.UndefOr[String]
  val moduleKind: String
  val verbosity: String
  val targetDirectory: String
  val scalaVersion: String
  val scalaJSVersion: String
  val libraryDependencies: js.Array[String]
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
          targetDirectory = js.Dynamic.literal(`type` = JSONSchema7TypeName.string),
          scalaVersion = js.Dynamic.literal(`type` = JSONSchema7TypeName.string),
          scalaJSVersion = js.Dynamic.literal(`type` = JSONSchema7TypeName.string),
          libraryDependencies = js.Dynamic.literal(
            `type` = JSONSchema7TypeName.array,
            items = js.Dynamic.literal(`type` = JSONSchema7TypeName.string)
          )
        )
      )
      .asInstanceOf[JSONSchema7]

  def defaults: Options = new Options {
    override val mainMethod = js.undefined
    override val moduleKind = "CommonJSModule"
    override val verbosity = "info"
    override val targetDirectory = "target"
    override val scalaVersion = "2.13.1"
    override val scalaJSVersion = "1.0.0"
    override val libraryDependencies = js.Array()
  }

  def get(context: LoaderContext, name: String): Try[Options] = {
    val options = getOptions(context)
    Try {
      validateOptions(schema, options, ValidationErrorConfiguration(name = name))
      js.Object.assign(defaults, options).asInstanceOf[Options]
    }
  }
}
