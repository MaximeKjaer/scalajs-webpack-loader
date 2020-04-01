package io.kjaer.scalajs.webpack

import io.kjaer.scalajs.webpack.DependencyFetch.{
  DependencyConflictException,
  FetchException,
  ResolutionException
}

import scala.scalajs.js
import scala.scalajs.js.annotation._
import typings.node.pathMod.{^ => path}
import typings.fsExtra.{mod => fs}
import typings.node.{Buffer, nodeStrings, childProcessMod => childProcess}
import typings.webpack.mod.loader.LoaderContext

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.Failure
import scala.util.Success

object Loader {
  val name = "scalajs-webpack-loader"

  @JSExportTopLevel("default")
  val loader: js.ThisFunction1[LoaderContext, String, Unit] =
    (self: LoaderContext, source: String) => {
      val callback = self.async().getOrElse {
        throw new Error("Async loaders are not supported")
      }
      def returnOutput(output: Buffer): Unit = callback(js.undefined, output, js.undefined)
      def returnError(err: Throwable): Unit = callback(
        typings.std.Error(err.getMessage),
        js.undefined,
        js.undefined
      )

      val options = Options.get(self, name).get // TODO better error handling

      val scalaFolder = self.resourcePath.split(path.sep).init.mkString(path.sep)
      val scalaFiles = fs
        .readdirSync(scalaFolder)
        .filter(_.endsWith(".scala"))
        .map(file => path.join(scalaFolder, file))

      scalaFiles.foreach(file => self.addDependency(file))

      implicit val logger: WebpackLogger = getLogger(
        WebpackLoggerOptions(name = name, level = options.verbosity)
      )

      val dependencies = Dependencies.default

      val currentDir = path.resolve(".")
      val targetDir =
        path.join(currentDir, options.targetDirectory, s"scala-${dependencies.scalaMinorVersion}")
      val classesDir = path.join(targetDir, "classes")
      val targetFile = path.join(targetDir, "bundle.js")
      val cacheDir = path.join(currentDir, ".cache")
      // val cacheDir = path.join(os.homedir(), ".ivy2/local")

      fs.ensureDirSync(classesDir)

      val downloadAndCompile = for {
        dependencyFiles <- DependencyFetch.fetchDependencies(dependencies, cacheDir)
        _ = logger.log("Compiling")
        compilationOutput <- compile(scalaFiles, classesDir, dependencyFiles)
        _ = logger.log("Linking")
        linkingOutput <- link(classesDir, targetFile, dependencyFiles, options)
        outputFile <- fs.promises.readFile(targetFile).toFuture
      } yield outputFile

      downloadAndCompile
        .onComplete {
          case Success(output) =>
            returnOutput(output)

          case Failure(err) =>
            err match {
              case ResolutionException(errors) =>
                logger.error("The following dependencies could not be resolved:")
                logger.error(errors.mkString("\n"))
              case DependencyConflictException(conflicts) =>
                logger.error("The following dependencies conflict:")
                logger.error(conflicts.mkString("\n"))
              case FetchException(errors) =>
                logger.error("Fetching could not be done for the following reasons:")
                logger.error(errors.mkString("\n"))
              case _ =>
                logger.error("An unknown error occurred")
                logger.error(err.getMessage)
            }
            returnError(err)
        }
    }

  private def compile(
      scalaFiles: Iterable[String],
      classesDir: String,
      dependencyFiles: DependencyFiles
  ): Future[String] = {
    val javaOptions = Seq("-cp", dependencyFiles.scalaCompiler.classpath, "scala.tools.nsc.Main")

    val plugin = Seq("-Xplugin:" + dependencyFiles.scalaJSCompiler.file)
    val destination = Seq("-d", classesDir)
    val classpath = Seq("-classpath", dependencyFiles.scalaJSLibrary.classpath)
    val scalaOptions = plugin ++ destination ++ classpath

    execCommand("java", javaOptions ++ scalaOptions ++ scalaFiles)
  }

  private def link(
      classesDir: String,
      targetFile: String,
      dependencyFiles: DependencyFiles,
      options: Options
  ): Future[String] = {
    val javaOptions = Seq("-cp", dependencyFiles.scalaJSCLI.classpath, "org.scalajs.cli.Scalajsld")

    val stdlib = Seq("--stdlib", dependencyFiles.scalaJSLibrary.file)
    val moduleKind = Seq("--moduleKind", options.moduleKind)
    val output = Seq("--output", targetFile)
    val mainMethod = options.mainMethod.map(Seq("--mainMethod", _)).getOrElse(Seq.empty)
    val scalajsldOptions = stdlib ++ moduleKind ++ output ++ mainMethod

    execCommand("java", javaOptions ++ scalajsldOptions :+ classesDir)
  }

  private def execCommand(command: String, options: Seq[String]): Future[String] = {
    val promise = Promise[String]()
    val stdout = new StringBuilder()
    val stderr = new StringBuilder()
    val process = childProcess.spawn(command, js.Array(options: _*))

    process
      .on_exit(
        nodeStrings.exit,
        (code, signals) => {
          if (code != null && code == 0d) promise.success(stdout.mkString)
          else
            promise.failure(
              js.JavaScriptException(
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

    promise.future
  }
}
