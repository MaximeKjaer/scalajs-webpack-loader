package io.kjaer.scalajs

import coursier.{Dependency, Module}

package object webpack {

  /**
    * A [[coursier.Dependency]] may have different fields before and after resolution. This makes it
    * hard to find a requested dependency in a map of resolved dependencies. Instead, we use
    * `(Module, String)` as a unique identifier of a dependency.
    */
  type DependencyName = (Module, String)

  def dependencyName(dependency: Dependency): DependencyName =
    (dependency.module, dependency.version)
}
