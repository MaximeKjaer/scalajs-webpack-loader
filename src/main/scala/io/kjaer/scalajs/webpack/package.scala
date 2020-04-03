package io.kjaer.scalajs

import coursier.util.Monad
import coursier.{Dependency, Module}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

package object webpack {

  /**
    * A [[coursier.Dependency]] may have different fields before and after resolution. This makes it
    * hard to find a requested dependency in a map of resolved dependencies. Instead, we use
    * `(Module, String)` as a unique identifier of a dependency.
    */
  type DependencyName = (Module, String)

  def dependencyName(dependency: Dependency): DependencyName =
    (dependency.module, dependency.version)

  /**
    * Coursier exposes a [[coursier.util.EitherT]] type that can help write for-comprehensions over `F[Either[A, B]]`.
    * This type needs implicit evidence that the higher-kinded type `F` is a monad, implemented as `implicit Monad[F]`.
    * scalajs-webpack-loader uses `Future[Either[A, B]]` in a few places, so it needs an implicit `Monad[Future]`.
    * The `EitherT` type is inspired by the `EitherT` available in the Cats library. You can read more about the type at:
    *
    *   https://typelevel.org/cats/datatypes/eithert.html
    */
  implicit val futureMonad: Monad[Future] = new Monad[Future] {
    override def point[A](a: A): Future[A] = Future(a)
    override def bind[A, B](elem: Future[A])(f: A => Future[B]): Future[B] = elem.flatMap(f)
  }
}
