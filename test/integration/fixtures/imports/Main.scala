object Main {
  def main(args: Array[String]): Unit = {
    // Foobaz does not need to be explicitly imported as it is part of the same module
    val foobaz42 = new Foobaz(42)
    print("Expecting 42: ")
    println(foobaz42.x)
    print("Expecting true: ")
    println(foobaz42.is42)

    val foobaz43 = new Foobaz(43)
    print("Expecting 43: ")
    println(foobaz43.x)
    print("Expecting false: ")
    println(foobaz43.is42)
  }
}
