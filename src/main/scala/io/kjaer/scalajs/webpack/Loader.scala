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
    val classesDirectory = ctx.options.classesDirectory
    val cacheDir = ctx.options.cacheDirectory
    val dependencies = ctx.options.dependencies
    val targetFile = path.join(targetDirectory, "bundle.js")
    val projectName = "FIXME-TMP-VALUE"

    fs.emptyDirSync(ctx.options.classesDirectory) // TODO do not empty dir if Bloop can do incremental compilation
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
      compilationOutput <- compile(scalaFiles, classesDirectory, projectDependencyFiles)
      linkingOutput <- link(classesDirectory, targetFile, projectDependencyFiles)
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
  )(implicit ctx: Context): EitherT[Future, LoaderException, DependencyFile] = {
    DependencyFetch.fetch(Seq(Bloop.Dependencies.bloopLauncher), cacheDir)(ctx.logger).map {
      case (resolution, files) =>
        DependencyFile.fromResolution(resolution, Bloop.Dependencies.bloopLauncher, files)
    }
  }

  private def compile(
      scalaFiles: Iterable[String],
      classesDir: String,
      projectDependencies: ProjectDependencyFiles
  )(implicit ctx: Context): EitherT[Future, LoaderException, String] = {
    val javaOptions =
      Seq(
        "-cp",
        projectDependencies.scalaCompiler.classpath.mkString(":"),
        "scala.tools.nsc.Main"
      )

    val plugin = Seq("-Xplugin:" + projectDependencies.scalaJSCompiler.jarPath)
    val destination = Seq("-d", classesDir)
    val classpath = Seq(
      "-classpath",
      DependencyFile
        .classpath(
          projectDependencies.scalaJSLibrary +: projectDependencies.libraryDependencies
        )
        .mkString(":")
    )
    val scalaOptions = plugin ++ ctx.options.scalacOptions ++ destination ++ classpath

    ctx.logger.operation("Compiling") {
      execCommand("java", javaOptions ++ scalaOptions ++ scalaFiles).leftMap(CompilerException)
    }
  }

  private def link(
      classesDir: String,
      targetFile: String,
      projectDependencies: ProjectDependencyFiles
  )(implicit ctx: Context): EitherT[Future, LoaderException, String] = {
    val javaOptions =
      Seq(
        "-cp",
        projectDependencies.scalaJSCLI.classpath.mkString(":"),
        "org.scalajs.cli.Scalajsld"
      )

    val stdlib = Seq("--stdlib", projectDependencies.scalaJSLibrary.jarPath)
    val moduleKind = Seq("--moduleKind", ctx.options.moduleKind)
    val output = Seq("--output", targetFile)
    val mainMethod = ctx.options.mainMethod.map(Seq("--mainMethod", _)).getOrElse(Seq.empty)
    val scalajsldOptions = stdlib ++ moduleKind ++ output ++ mainMethod

    val classpath = DependencyFile.classpath(projectDependencies.libraryDependencies) :+ classesDir

    ctx.logger.operation("Linking") {
      execCommand("java", javaOptions ++ scalajsldOptions ++ classpath).leftMap(LinkerException)
    }
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
