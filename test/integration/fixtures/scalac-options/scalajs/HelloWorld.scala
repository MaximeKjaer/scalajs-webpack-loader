import scalajs.js.annotation._

@JSExportTopLevel("HelloWorld")
object HelloWorld {
  @JSExport
  def sayHello(): Unit = {
    println(1 to 10 toList) // only compiles with `-language:postfixOps`
  }
}
