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
  // Specifies the name of the logger to create. This value will be part of the log output prefix.
  val name: String
}

object WebpackLoggerOptions {
  def apply(name: String): WebpackLoggerOptions =
    js.Dynamic.literal(name = name).asInstanceOf[WebpackLoggerOptions]
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
  def error(message: js.UndefOr[String]): Unit = js.native

  /**
    * For warnings
    */
  def warn(message: js.UndefOr[String]): Unit = js.native

  /**
    * For important messages
    */
  def info(message: js.UndefOr[String]): Unit = js.native

  /**
    * For unimportant messages
    */
  def log(message: js.UndefOr[String]): Unit = js.native

  /**
    * For debugging information. These messages are displayed only when user had opted-in to see
    * debug logging for specific modules.
    */
  def debug(message: js.UndefOr[String]): Unit = js.native

  /**
    * To display a stack trace. Displayed like [[debug]]
    */
  def trace(message: js.UndefOr[String]): Unit = js.native
}
