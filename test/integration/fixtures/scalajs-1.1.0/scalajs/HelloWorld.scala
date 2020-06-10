import scalajs.js.annotation._

@JSExportTopLevel("HelloWorld")
object HelloWorld {
  @JSExport
  def sayHello(): Unit = {
    println("Hello world from Scala.js 1.1.0!")
  }
}
