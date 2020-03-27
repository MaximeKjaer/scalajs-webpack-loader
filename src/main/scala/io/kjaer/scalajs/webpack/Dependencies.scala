package io.kjaer.scalajs.webpack

import coursier._

case class Dependencies(scalaVersion: String, scalaJSVersion: String) {
  val scalaMinorVersion = scalaVersion.take(scalaVersion.lastIndexOf("."))

  val scalaCompiler =
    Dependency(Module(org"org.scala-lang", name"scala-compiler"), scalaVersion)

  val scalaJSCompiler = Dependency(
    Module(org"org.scala-js", ModuleName(s"scalajs-compiler_$scalaVersion")),
    scalaJSVersion
  )
  val scalaJSLib = Dependency(
    Module(org"org.scala-js", ModuleName(s"scalajs-library_$scalaMinorVersion")),
    scalaJSVersion
  )

  def toSeq: Seq[Dependency] = Seq(scalaCompiler, scalaJSCompiler, scalaJSLib)
}

object Dependencies {
  def default: Dependencies = Dependencies(scalaVersion = "2.13.1", scalaJSVersion = "1.0.0")
}
