package io.kjaer.scalajs.webpack

import coursier.{Dependency, moduleString => mod}
import bloop.config.Config
import typings.node.pathMod.{^ => path}
import typings.node.processMod.{^ => process}

object Bloop {
  object Dependencies {
    val version = "1.4.1"

    val bloopLauncher = Dependency(mod"ch.epfl.scala:bloop-launcher_2.12", version)
    val bloopFrontend = Dependency(mod"ch.epfl.scala:bloop-frontend_2.12", version)

    val All = Seq(bloopLauncher, bloopFrontend)
  }

  case class DependencyFiles(bloopLauncher: DependencyFile, bloopFrontend: DependencyFile)

  object DependencyFiles {
    def fromResolution(
        resolution: coursier.Resolution,
        files: Map[DependencyId, String]
    ): DependencyFiles = {
      def dependencyFile(dependency: Dependency): DependencyFile =
        DependencyFile.fromResolution(resolution, dependency, files)

      DependencyFiles(
        bloopLauncher = dependencyFile(Dependencies.bloopLauncher),
        bloopFrontend = dependencyFile(Dependencies.bloopFrontend)
      )
    }
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
      version = Bloop.Dependencies.version,
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
            options = s"-Xplugin:${dependencies.scalaJSCompiler.jarPath}" +: ctx.options.scalacOptions.toList,
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
              mode = Config.LinkerMode.Debug, // todo expose this option
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
