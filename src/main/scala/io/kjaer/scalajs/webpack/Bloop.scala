package io.kjaer.scalajs.webpack

import coursier.{dependencyString => dep}

object Bloop {
  object Dependencies {
    val bloopLauncher = dep"ch.epfl.scala:bloop-launcher_2.12:1.4.1"
  }
}
