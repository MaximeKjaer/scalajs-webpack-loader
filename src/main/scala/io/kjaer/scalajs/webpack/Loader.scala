package io.kjaer.scalajs.webpack

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.scalajs.js.annotation.JSExportStatic

@js.native
trait Options extends js.Object {
  val name: String
}

@js.native
@JSImport("loader-utils", JSImport.Namespace)
object LoaderUtils extends js.Object {
  def getOptions(self: Any): Options = js.native
}

object Loader {
  @JSExportTopLevel("loader")
  val loader: js.ThisFunction1[Any, String, String] = (self: Any, source: String) => {
    val options = LoaderUtils.getOptions(self)
    val replaced = source.replaceAll("""\[name\]""", options.name)
    s"export default ${js.JSON.stringify(replaced)};"
  }
}
