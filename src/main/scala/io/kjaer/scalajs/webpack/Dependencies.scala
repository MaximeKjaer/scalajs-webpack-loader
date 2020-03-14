package io.kjaer.scalajs.webpack

import coursier._
import coursier.cache.Cache
import coursier.util.{Artifact, Gather, Task}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

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

  case class ResolutionException(errors: Seq[((Module, String), Seq[String])])
      extends Exception("Could not get metadata about some dependencies")

  case class DependencyConflictException(conflicts: Set[Dependency])
      extends Exception("Conflicts were found in the dependencies")

  def resolve(): Future[Resolution] =
    Resolve()
      .withRepositories(Seq(MavenRepository("https://repo1.maven.org/maven2")))
      .withDependencies(Seq(scalaCompiler, scalaJSCompiler, scalaJSLib))
      .future()
      .transform {
        case success @ Success(resolution) =>
          if (resolution.errors.nonEmpty)
            Failure(ResolutionException(resolution.errors))
          if (resolution.conflicts.nonEmpty)
            Failure(DependencyConflictException(resolution.conflicts))
          else
            success
        case failure => failure
      }

  def fetch(artifacts: Seq[Artifact]): Future[Seq[Either[String, String]]] =
    Gather[Task]
      .gather(artifacts.map(Cache.default.fetch(_).run))
      .future()
}
