import scalajs.js.annotation._

@JSExportTopLevel("HelloWorld")
object HelloWorld {
  @JSExport
  def sayHello(): Unit = {
    println("Hello world from Scala.js 1.1.0!")
  }

  def main(args: Array[String]): Unit = {
    // TODO remove this when bloop#1304 is fixed
  }
}
