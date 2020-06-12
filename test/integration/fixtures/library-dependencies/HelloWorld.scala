import scalajs.js.annotation._
import upickle.default._

@JSExportTopLevel("HelloWorld")
object HelloWorld {
  @JSExport
  def sayHello(): Unit = {
    println(write(Seq(1, 2, 3)))
  }

  def main(args: Array[String]): Unit = {
    // TODO remove this when bloop#1304 is fixed
  }
}
