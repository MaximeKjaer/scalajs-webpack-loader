package io.kjaer.scalajs.webpack

import scala.scalajs.js
import scala.scalajs.js.annotation._
import typings.loaderUtils.mod.getOptions
import typings.webpack.mod.loader.LoaderContext

@js.native
trait Options extends js.Object {
  val name: String
}

object Loader {
  @JSExportTopLevel("loader")
  val loader: js.ThisFunction1[LoaderContext, String, String] =
    (self: LoaderContext, source: String) => {
      val options = getOptions(self).asInstanceOf[Options]
      val replaced = source.replaceAll("""\[name\]""", options.name)
      s"export default ${js.JSON.stringify(replaced)};"
    }
}
