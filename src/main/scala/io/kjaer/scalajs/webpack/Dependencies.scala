package io.kjaer.scalajs.webpack

import coursier._
import coursier.cache.Cache
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Dependencies {
  val scalaPatchVersion = "2.13.1"
  val scalaMinorVersion = "2.13"
  val scalaJSVersion = "1.0.0-RC1"

  val scalaCompiler =
    Dependency(Module(org"org.scala-lang", name"scala-compiler"), scalaPatchVersion)

  val scalaJSCompiler = Dependency(
    Module(org"org.scala-js", ModuleName(s"scalajs-compiler_$scalaPatchVersion")),
    scalaJSVersion
  )

  val scalaJSLib = Dependency(
    Module(org"org.scala-js", ModuleName(s"scalajs-library_$scalaMinorVersion")),
    scalaJSVersion
  )

  def resolve(): Future[Resolution] = {
    val repositories = Seq(
      MavenRepository("https://repo1.maven.org/maven2")
    )

    val fetch = ResolutionProcess.fetch(repositories, Cache.default.fetch)

    Resolve()
      .withDependencies(Seq(scalaCompiler, scalaJSCompiler, scalaJSLib))
      .future()
  }
}
