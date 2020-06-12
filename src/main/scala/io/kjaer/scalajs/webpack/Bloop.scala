package io.kjaer.scalajs.webpack

import coursier.{dependencyString => dep}
import bloop.config.Config

import typings.node.pathMod.{^ => path}
import typings.node.processMod.{^ => process}

object Bloop {
  object Dependencies {
    val bloopLauncher = dep"ch.epfl.scala:bloop-launcher_2.12:1.4.1"
  }

  def bloopDirectory: String =
    path.join(path.resolve("."), ".bloop")

  def configFile(projectName: String): String =
    path.join(bloopDirectory, projectName + ".json")

  def exportConfig(
      projectName: String,
      scalaDirectory: String,
      dependencies: ProjectDependencyFiles
  )(
      implicit ctx: Context
  ): Config.File = {
    val scalaCompiler = ctx.options.dependencies.scalaCompiler

    Config.File(
      version = Config.File.LatestVersion,
      project = Config.Project(
        name = projectName,
        directory = ctx.options.currentDirectory,
        workspaceDir = Some(ctx.options.currentDirectory),
        sources = List(scalaDirectory),
        sourcesGlobs = None,
        sourceRoots = None,
        dependencies = List.empty,
        classpath = dependencies.classpath.toList,
        out = ctx.options.targetDirectory,
        classesDir = ctx.options.classesDirectory,
        resources = None,
        scala = Some(
          Config.Scala(
            organization = scalaCompiler.module.organization.value,
            name = scalaCompiler.module.name.value,
            version = scalaCompiler.version,
            options = ctx.options.scalacOptions.toList,
            jars = dependencies.scalaCompiler.classpath.toList,
            analysis = None,
            setup = None
          )
        ),
        java = None,
        sbt = None,
        test = None,
        platform = Some(
          Config.Platform.Js(
            config = Config.JsConfig(
              version = ctx.options.versions.scalaJSVersion,
              mode = Config.LinkerMode.Release, // todo expose this option
              kind = Config.ModuleKindJS.CommonJSModule,
              emitSourceMaps = false, // todo expose this option
              jsdom = None,
              output = Some(ctx.options.targetFile),
              nodePath = Some(process.execPath),
              toolchain = List.empty
            ),
            mainClass = None
          )
        ),
        resolution = None, // todo export Coursier resolution
        tags = None
      )
    )
  }
}
