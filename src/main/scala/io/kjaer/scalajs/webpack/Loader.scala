package io.kjaer.scalajs.webpack

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.|

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.Failure
import scala.util.Success

import coursier.util.EitherT

import typings.node.pathMod.{^ => path}
import typings.fsExtra.{mod => fs}
import typings.node.{Buffer, nodeStrings, childProcessMod => childProcess}
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

  private def load(self: LoaderContext): EitherT[Future, LoaderException, Buffer] =
    for {
      options <- EitherT.fromEither(Options.get(self, name))
      parsedOptions <- EitherT.fromEither(ParsedOptions.parse(options))
      logger = LoaderLogger(getLogger(WebpackLoggerOptions(name = name, level = options.verbosity)))
      ctx = Context(self, parsedOptions, logger)
      buffer <- downloadAndCompile(ctx)
    } yield buffer

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
      _ <- writeFile(Bloop.configFile(projectName), bloop.config.write(bloopConfig))
      launchOutput <- launchBloop(bloopFiles.bloopLauncher)
      compileOutput <- compileWithBloop(bloopFiles.bloopFrontend, projectName)
      linkOutput <- linkWithBloop(bloopFiles.bloopFrontend, projectName)
      outputFile <- readFile(targetFile)
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
    val javaOptions = Seq(
      "-cp",
      launcher.classpath.mkString(":"),
      "bloop.launcher.Launcher"
    )
    val bloopOptions = Seq(Bloop.Dependencies.version, "--skip-bsp-connection")

    ctx.logger.operation("Launching bloop") {
      execCommand("java", javaOptions ++ bloopOptions)
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
  ): EitherT[Future, String, String] = {
    execCommand("java", Seq("-cp", frontend.classpath.mkString(":"), "bloop.Cli") ++ command)
  }

  private def execCommand(
      command: String,
      options: Seq[String]
  ): EitherT[Future, String, String] = {
    val promise = Promise[Either[String, String]]()
    val stdout = new StringBuilder()
    val stderr = new StringBuilder()
    val process = childProcess.spawn(command, js.Array(options: _*))

    process
      .on_exit(
        nodeStrings.exit,
        (code, signals) => {
          if (code.asInstanceOf[Double] == 0d)
            promise.success(Right(stdout.mkString))
          else
            promise.success(
              Left(
                stderr.mkString + s"\nExited with code $code (because of $signals)"
              )
            )
        }
      )

    process.stdout_ChildProcessWithoutNullStreams
      .on_data(nodeStrings.data, (buffer) => {
        val data = buffer.asInstanceOf[typings.node.Buffer].toString()
        stdout.appendAll(data)
      })

    process.stderr_ChildProcessWithoutNullStreams
      .on_data(nodeStrings.data, (buffer) => {
        val data = buffer.asInstanceOf[typings.node.Buffer].toString()
        stderr.appendAll(data)
      })

    EitherT(promise.future)
  }

  private def readFile(path: String): EitherT[Future, LoaderException, Buffer] = {
    EitherT(
      fs.readFile(path)
        .toFuture
        .map(Right(_))
        .recover(err => Left(FileReadException(path, err.getMessage)))
    )
  }

  private def writeFile(
      path: String,
      contents: String
  ): EitherT[Future, LoaderException, Unit] = {
    EitherT(
      fs.writeFile(path, contents)
        .toFuture
        .map(Right(_))
        .recover(err => Left(FileWriteException(path, err.getMessage)))
    )
  }
}
