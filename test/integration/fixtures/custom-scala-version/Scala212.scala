import scalajs.js.annotation._

@JSExportTopLevel("Scala212")
object Scala212 {
  @JSExport
  def sayHello(): Unit = {
    // Traversable is available in 2.12, not in 2.13.
    // If this compiles, it means we've used scalac 2.12.
    val x: Traversable[String] = Seq("Hello", "world")
    println(x.mkString(" "))
  }
}
