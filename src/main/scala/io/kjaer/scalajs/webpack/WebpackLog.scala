package io.kjaer.scalajs.webpack

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("webpack-log", JSImport.Namespace)
object getLogger extends js.Object {
  def apply(options: WebpackLoggerOptions): WebpackLogger = js.native
}

@js.native
trait WebpackLoggerOptions extends js.Object {

  /**
    * Specifies the name of the logger to create. This value will be part of the log output prefix.
    */
  val name: String

  /**
    * Specifies the level the logger should use. A logger will not produce output for any log level
    * beneath the specified level. Valid level names, and their order are:
    *
    *     "trace", "debug", "info", "warn", "error", "silent".
    *
    * For example, If a level was passed as { level: 'warn'} then only calls to warn and error will be
    * displayed in the terminal.
    */
  val level: js.UndefOr[String]

}

object WebpackLoggerOptions {
  def apply(
      name: String,
      level: js.UndefOr[String] = js.undefined
  ): WebpackLoggerOptions =
    js.Dynamic.literal(name = name, level = level).asInstanceOf[WebpackLoggerOptions]
}

/**
  * `webpack-log` logger.
  *
  * Note that Webpack also has a logger since v4.39.0, but in order to support older versions of
  * Webpack, we use the external `webpack-log` logger.
  *
  * @see https://github.com/shellscape/webpack-log/blob/master/README.md
  */
@js.native
trait WebpackLogger extends js.Object {

  /**
    * For error messages
    */
  def error(message: js.Any, optionalParams: js.Any*): Unit = js.native

  /**
    * For warnings
    */
  def warn(message: js.Any, optionalParams: js.Any*): Unit = js.native

  /**
    * For important messages
    */
  def info(message: js.Any, optionalParams: js.Any*): Unit = js.native

  /**
    * For unimportant messages
    */
  def log(message: js.Any, optionalParams: js.Any*): Unit = js.native

  /**
    * For debugging information. These messages are displayed only when user had opted-in to see
    * debug logging for specific modules.
    */
  def debug(message: js.Any, optionalParams: js.Any*): Unit = js.native

  /**
    * To display a stack trace. Displayed like [[debug]]
    */
  def trace(message: js.Any, optionalParams: js.Any*): Unit = js.native
}
