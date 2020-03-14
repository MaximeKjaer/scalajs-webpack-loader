package io.kjaer.scalajs.webpack

import scala.scalajs.js
import scala.scalajs.js.annotation._
import typings.loaderUtils.mod.getOptions
import typings.webpack.mod.loader.LoaderContext

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

@js.native
trait Options extends js.Object {
  val name: String
}

object Loader {
  @JSExportTopLevel("default")
  val loader: js.ThisFunction1[LoaderContext, String, Unit] =
    (self: LoaderContext, source: String) => {
      val callback = self.async().getOrElse {
        throw new Error("Async loaders are not supported")
      }

      val result = output(self, source)
      callback(js.undefined, result, js.undefined)
    }

  def output(self: LoaderContext, source: String): String = {
    val options = getOptions(self).asInstanceOf[Options]
    val replaced = source.replaceAll("""\[name\]""", options.name)
    s"export default ${js.JSON.stringify(replaced)};"
  }

  def getDependencies(): Future[Seq[Either[String, String]]] =
    Dependencies
      .resolve()
      .flatMap(resolution => Dependencies.fetch(resolution.artifacts()))
  /* .onComplete {
        case Success(artifacts) =>
          val (errors, files) = artifacts.partition(_.isLeft)
          if (errors.nonEmpty) logger.error(errors.mkString("\n"))
          if (files.nonEmpty) logger.info(files.mkString("\n"))
        case Failure(exception) =>
          callback(
            js.JavaScriptException(exception).asInstanceOf[typings.std.Error],
            js.undefined,
            js.undefined
          )
      } */
}
