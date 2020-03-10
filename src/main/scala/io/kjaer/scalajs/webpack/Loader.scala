package io.kjaer.scalajs.webpack

import scala.scalajs.js
import scala.scalajs.js.annotation._
import typings.loaderUtils.mod.getOptions
import typings.webpack.mod.loader.LoaderContext
import scala.concurrent.ExecutionContext.Implicits.global
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

      val options = getOptions(self).asInstanceOf[Options]
      val replaced = source.replaceAll("""\[name\]""", options.name)
      val output = s"export default ${js.JSON.stringify(replaced)};"

      Dependencies.resolve().onComplete {
        case Success(value) =>
          println(value)
          callback(js.undefined, output, js.undefined)
        case Failure(exception) =>
          callback(
            new js.JavaScriptException(exception).asInstanceOf[typings.std.Error],
            js.undefined,
            js.undefined
          )
      }
    }
}
