import scalajs.js.annotation._

@JSExportTopLevel("HelloWorld")
object HelloWorld {
  @JSExport
  def sayHello(): Unit = {
    println("Hello world!")
  }

  def main(args: Seq[String]): Unit = {
    // TODO remove this when bloop#1304 is fixed
  }
}
