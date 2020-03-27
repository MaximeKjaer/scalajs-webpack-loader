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
import typings.node.{nodeStrings, childProcessMod => childProcess}
import typings.loaderUtils.mod.getOptions
import typings.webpack.mod.loader.LoaderContext

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.Failure
import scala.util.Success

object Loader {
  @JSExportTopLevel("default")
  val loader: js.ThisFunction1[LoaderContext, String, Unit] =
    (self: LoaderContext, source: String) => {
      val callback = self.async().getOrElse {
        throw new Error("Async loaders are not supported")
      }
      def returnOutput(output: String): Unit = callback(js.undefined, output, js.undefined)
      def returnError(err: Throwable): Unit = callback(
        typings.std.Error(err.getMessage),
        js.undefined,
        js.undefined
      )

      val options = getOptions(self).asInstanceOf[Options]

      val scalaFolder = self.resourcePath.split(path.sep).init.mkString(path.sep)
      val scalaFiles = fs
        .readdirSync(scalaFolder)
        .filter(_.endsWith(".scala"))
        .map(file => path.join(scalaFolder, file))

      implicit val logger: WebpackLogger = getLogger(
        WebpackLoggerOptions(name = "scalajs-loader", level = "debug")
      )

      val dependencies = Dependencies.default

      val currentDir = path.resolve(".")
      val targetDir =
        path.join(currentDir, "target", s"scala-${dependencies.scalaMinorVersion}", "classes")
      val cacheDir = path.join(currentDir, ".cache")
      // val cacheDir = path.join(os.homedir(), ".ivy2/local")

      fs.ensureDirSync(targetDir)

      DependencyFetch
        .fetchDependencies(dependencies, cacheDir)
        .flatMap { dependencyFiles =>
          execCommand(
            "java",
            "-cp",
            dependencyFiles.scalaCompiler.values.mkString(":"),
            "scala.tools.nsc.Main",
            "-Xplugin:" + dependencyFiles.scalaJSCompiler,
            "-d",
            targetDir,
            "-classpath",
            dependencyFiles.scalaJSLibrary.values.mkString(":"),
            scalaFiles.mkString(" ")
          )
        }
        .onComplete {
          case Success(output) =>
            println("command output!")
            println(output)
            returnOutput(jsOutput(self, source))

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
            }
            returnError(err)
        }
    }

  def jsOutput(self: LoaderContext, source: String): String = {
    val options = getOptions(self).asInstanceOf[Options]
    val replaced = source.replaceAll("""\[name\]""", options.name)
    s"export default ${js.JSON.stringify(replaced)};"
  }

  private def execCommand(command: String, options: String*): Future[String] = {
    val promise = Promise[String]()
    val output = new StringBuilder()
    val process = childProcess.spawn(command, js.Array(options: _*))

    process
      .on_close(
        nodeStrings.close,
        (code, signals) => {
          println("CLOSE")
          if (code == 0) promise.success(output.mkString)
          else promise.failure(js.JavaScriptException(s"non-zero code $code because of $signals"))
        }
      )

    process.stdout_ChildProcessWithoutNullStreams
      .on_data(nodeStrings.data, (buffer) => {
        println("DATA")
        val data = buffer.asInstanceOf[typings.node.Buffer].toString()
        output.appendAll(data)
      })

    process.stderr_ChildProcessWithoutNullStreams
      .on_data(nodeStrings.data, (buffer) => {
        val data = buffer.asInstanceOf[typings.node.Buffer].toString()
        println(s"ERR DATA: $data")
      })

    promise.future
  }
}
