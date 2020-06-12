package io.kjaer.scalajs.webpack

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.|

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success

import coursier.util.EitherT

import typings.node.pathMod.{^ => path}
import typings.fsExtra.{mod => fs}
import typings.node.Buffer
import typings.webpack.mod.loader.LoaderContext
import typings.sourceMap.mod.RawSourceMap

object Loader {
  val name = "scalajs-webpack-loader"
  @JSExportTopLevel("default")
  val loader: js.ThisFunction1[LoaderContext, String, Unit] =
    (self: LoaderContext, source: String) => {
      type loaderCallback = js.Function3[
        /* err */ js.UndefOr[js.Error | Null],
        /* content */ js.UndefOr[String | Buffer],
        /* sourceMap */ js.UndefOr[RawSourceMap],
        Unit
      ]
      val callback = self.async().asInstanceOf[js.UndefOr[loaderCallback]].getOrElse {
        throw new Error("Async loaders are not supported")
      }

      load(self).run.onComplete {
        case Failure(err) =>
          err.printStackTrace()
          callback(js.Error(err.getMessage), js.undefined, js.undefined)
        case Success(Left(err)) =>
          callback(js.Error(err.getMessage), js.undefined, js.undefined)
        case Success(Right(output)) =>
          callback(js.undefined, output, js.undefined)
      }
    }

  private def load(self: LoaderContext): EitherT[Future, LoaderException, Buffer] = {
    for {
      options <- EitherT.fromEither(Options.get(self, name))
      parsedOptions <- EitherT.fromEither(ParsedOptions.parse(options))
      logger = LoaderLogger(getLogger(WebpackLoggerOptions(name = name, level = options.verbosity)))
      ctx = Context(self, parsedOptions, logger)
      buffer <- downloadAndCompile(ctx)
    } yield buffer
  }

  private def downloadAndCompile(
      implicit ctx: Context
  ): EitherT[Future, LoaderException, Buffer] = {
    val scalaDirectory = ctx.loader.resourcePath.split(path.sep).init.mkString(path.sep)
    val scalaFiles = fs
      .readdirSync(scalaDirectory)
      .filter(_.endsWith(".scala"))
      .map(file => path.join(scalaDirectory, file))

    scalaFiles.foreach(ctx.loader.addDependency)

    val targetDirectory = ctx.options.targetDirectory
    val cacheDir = ctx.options.cacheDirectory
    val dependencies = ctx.options.dependencies
    val targetFile = path.join(targetDirectory, "bundle.js")
    val projectName = "FIXME-TMP-VALUE"

    fs.ensureDirSync(ctx.options.classesDirectory)
    fs.ensureDirSync(Bloop.bloopDirectory)

    // These can run concurrently:
    val futureProjectDependencyFiles =
      fetchProjectDependencies(dependencies, cacheDir)
    val futureBloopFiles = fetchBloop(cacheDir)

    for {
      projectDependencyFiles <- futureProjectDependencyFiles
      bloopFiles <- futureBloopFiles
      bloopConfig = Bloop.exportConfig(
        projectName = projectName,
        scalaDirectory = scalaDirectory,
        dependencies = projectDependencyFiles
      )
      _ <- NodeUtils.writeFile(Bloop.configFile(projectName), bloop.config.write(bloopConfig))
      _ <- launchBloop(bloopFiles.bloopLauncher)
      _ <- compileWithBloop(bloopFiles.bloopFrontend, projectName)
      _ <- linkWithBloop(bloopFiles.bloopFrontend, projectName)
      outputFile <- NodeUtils.readFile(targetFile)
    } yield outputFile
  }

  private def fetchProjectDependencies(dependencies: ProjectDependencies, cacheDir: String)(
      implicit ctx: Context
  ): EitherT[Future, LoaderException, ProjectDependencyFiles] = {
    DependencyFetch.fetch(dependencies.toSeq, cacheDir)(ctx.logger).map {
      case (resolution, files) =>
        ProjectDependencyFiles.fromResolution(resolution, dependencies, files)
    }
  }

  private def fetchBloop(
      cacheDir: String
  )(implicit ctx: Context): EitherT[Future, LoaderException, Bloop.DependencyFiles] = {
    DependencyFetch.fetch(Bloop.Dependencies.All, cacheDir)(ctx.logger).map {
      case (resolution, files) => Bloop.DependencyFiles.fromResolution(resolution, files)
    }
  }

  private def launchBloop(
      launcher: DependencyFile
  )(implicit ctx: Context): EitherT[Future, LoaderException, String] = {
    // See https://scalacenter.github.io/bloop/docs/launcher-reference#usage
    // See https://github.com/scalacenter/bloop/blob/master/launcher/src/main/scala/bloop/launcher/Launcher.scala
    val bloopOptions = Seq(Bloop.Dependencies.version, "--skip-bsp-connection")
    ctx.logger.operation("Launching bloop") {
      NodeUtils
        .execJava(launcher.classpath, "bloop.launcher.Launcher", bloopOptions)
        .leftMap(BloopLaunchException)
    }
  }

  private def compileWithBloop(
      frontend: DependencyFile,
      projectName: String
  )(implicit ctx: Context): EitherT[Future, LoaderException, String] = {
    ctx.logger.operation("Compiling with bloop") {
      bloopRun(frontend, Seq("compile", projectName)).leftMap(CompilerException)
    }
  }

  private def linkWithBloop(
      frontend: DependencyFile,
      projectName: String
  )(implicit ctx: Context): EitherT[Future, LoaderException, String] = {
    ctx.logger.operation("Linking with bloop") {
      bloopRun(frontend, Seq("link", projectName)).leftMap(LinkerException)
    }
  }

  private def bloopRun(
      frontend: DependencyFile,
      command: Seq[String]
  ): EitherT[Future, String, String] =
    NodeUtils.execJava(frontend.classpath, "bloop.Cli", command)
}
