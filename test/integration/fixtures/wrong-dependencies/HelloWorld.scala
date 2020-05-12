import scalajs.js.annotation._
import upickle.default._

@JSExportTopLevel("HelloWorld")
object HelloWorld {
  @JSExport
  def sayHello(): Unit = {
    println(write(Seq(1, 2, 3)))
  }
}
