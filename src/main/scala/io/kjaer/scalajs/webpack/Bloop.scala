package io.kjaer.scalajs.webpack

import coursier.{dependencyString => dep}

import scala.scalajs.js

object Bloop {
  object Dependencies {
    val bloopLauncher = dep"ch.epfl.scala:bloop-launcher_2.12:1.4.1"
  }

  trait Config extends js.Object {
    val version: String
    val project: ProjectConfig
  }

  trait ProjectConfig extends js.Object {
    val name: String
    val directory: String
    val sources: js.Array[String]
    val dependencies: js.Array[String]
    val classpath: js.Array[String]
    val out: String
    val classesDir: String
  }
}
