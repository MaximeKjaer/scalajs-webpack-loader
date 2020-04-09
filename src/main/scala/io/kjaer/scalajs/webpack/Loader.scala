package io.kjaer.scalajs.webpack

import scala.scalajs.js
import scala.scalajs.js.annotation._
import typings.node.pathMod.{^ => path}
import typings.fsExtra.{mod => fs}
import typings.node.{Buffer, nodeStrings, childProcessMod => childProcess}
import typings.webpack.mod.loader.LoaderContext
import coursier.util.EitherT
import typings.sourceMap.mod.RawSourceMap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js.|
import scala.util.Failure
import scala.util.Success

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
      dependencies <- EitherT.fromEither(Dependencies.fromOptions(options))
      buffer <- downloadAndCompile(self, options, dependencies)
    } yield buffer

  private def downloadAndCompile(
      self: LoaderContext,
      options: Options,
      dependencies: Dependencies
  ): EitherT[Future, LoaderException, Buffer] = {
    implicit val logger: WebpackLogger = getLogger(
      WebpackLoggerOptions(name = name, level = options.verbosity)
    )

    val scalaFolder = self.resourcePath.split(path.sep).init.mkString(path.sep)
    val scalaFiles = fs
      .readdirSync(scalaFolder)
      .filter(_.endsWith(".scala"))
      .map(file => path.join(scalaFolder, file))
    val currentDir = path.resolve(".")
    val targetDir =
      path.join(currentDir, options.targetDirectory, s"scala-${dependencies.scalaBinVersion}")
    val classesDir = path.join(targetDir, "classes")
    val targetFile = path.join(targetDir, "bundle.js")
    val cacheDir = path.join(currentDir, ".cache")
    // val cacheDir = path.join(os.homedir(), ".ivy2/local")

    logger.info("Fetching dependencies")
    for {
      _ <- EitherT.point(prepareFiles(self, scalaFiles, classesDir))
      dependencyFiles <- DependencyFetch.fetchDependencies(dependencies, cacheDir)
      _ = logger.info("Compiling")
      compilationOutput <- compile(scalaFiles, classesDir, dependencyFiles)
      _ = logger.info("Linking")
      linkingOutput <- link(classesDir, targetFile, dependencyFiles, options)
      outputFile <- readFile(targetFile)
    } yield outputFile
  }

  private def prepareFiles(
      self: LoaderContext,
      scalaFiles: Iterable[String],
      classesDir: String
  ): Future[Unit] = {
    scalaFiles.foreach(self.addDependency)
    fs.promises
      .rmdir(classesDir)
      .toFuture
      .flatMap(_ => fs.ensureDir(classesDir).asInstanceOf[js.Promise[Unit]].toFuture)
  }

  private def compile(
      scalaFiles: Iterable[String],
      classesDir: String,
      dependencyFiles: DependencyFiles
  ): EitherT[Future, LoaderException, String] = {
    val javaOptions =
      Seq("-cp", DependencyFiles.classpath(dependencyFiles.scalaCompiler), "scala.tools.nsc.Main")

    val plugin = Seq("-Xplugin:" + dependencyFiles.scalaJSCompiler.file)
    val destination = Seq("-d", classesDir)
    val classpath = Seq(
      "-classpath",
      DependencyFiles.classpath(
        dependencyFiles.scalaJSLibrary +: dependencyFiles.libraryDependencies
      )
    )
    val scalaOptions = plugin ++ destination ++ classpath

    execCommand("java", javaOptions ++ scalaOptions ++ scalaFiles).leftMap(CompilerException)
  }

  private def link(
      classesDir: String,
      targetFile: String,
      dependencyFiles: DependencyFiles,
      options: Options
  ): EitherT[Future, LoaderException, String] = {
    val javaOptions =
      Seq("-cp", DependencyFiles.classpath(dependencyFiles.scalaJSCLI), "org.scalajs.cli.Scalajsld")

    val stdlib = Seq("--stdlib", dependencyFiles.scalaJSLibrary.file)
    val moduleKind = Seq("--moduleKind", options.moduleKind)
    val output = Seq("--output", targetFile)
    val mainMethod = options.mainMethod.map(Seq("--mainMethod", _)).getOrElse(Seq.empty)
    val scalajsldOptions = stdlib ++ moduleKind ++ output ++ mainMethod

    val classpath = dependencyFiles.libraryDependencies.flatMap(_.allFiles) :+ classesDir

    execCommand("java", javaOptions ++ scalajsldOptions ++ classpath).leftMap(LinkerException)
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
          if (code != null && code == 0d)
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

  private def readFile(file: String): EitherT[Future, LoaderException, Buffer] =
    EitherT(
      fs.promises
        .readFile(file)
        .toFuture
        .map(Right(_))
        .recover(err => Left(FileReadException(file, err.getMessage)))
    )
}
