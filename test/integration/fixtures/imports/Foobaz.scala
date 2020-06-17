import scala.scalajs.js
import scala.scalajs.js.annotation._

// Import Foo from ./bar.js and model it as the Foobaz Scala.js class.
// This is the example from https://www.scala-js.org/doc/interoperability/facade-types.html
@js.native
@JSImport("./bar.js", "Foo")
class Foobaz(val x: Int) extends js.Object {
  def is42(): Boolean = js.native
}
