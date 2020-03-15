package io.kjaer.scalajs.webpack

import coursier.Dependency
import io.kjaer.scalajs.webpack.FetchDependencies.{
  DependencyConflictException,
  FetchException,
  ResolutionException
}

import scala.scalajs.js
import scala.scalajs.js.annotation._
import typings.node.{fsMod => fs}
import typings.node.pathMod.{^ => path}
import typings.loaderUtils.mod.getOptions
import typings.node.fsMod.MakeDirectoryOptions
import typings.webpack.mod.loader.LoaderContext

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
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
        js.JavaScriptException(err).asInstanceOf[typings.std.Error],
        js.undefined,
        js.undefined
      )

      val logger = getLogger(WebpackLoggerOptions(name = "scalajs-loader"))
      val cacheDir = path.join(path.resolve("."), ".cache")
      // val cacheDir = path.join(os.homedir(), ".ivy2/local")

      downloadDependencies(Dependencies.default.toSeq, cacheDir).onComplete {
        case Success(map) =>
          logger.info(map.toString)
          returnOutput(output(self, source))

        case Failure(err) =>
          logger.error(err.getMessage())
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
          }
          returnError(err)
      }

    }

  def output(self: LoaderContext, source: String): String = {
    val options = getOptions(self).asInstanceOf[Options]
    val replaced = source.replaceAll("""\[name\]""", options.name)
    s"export default ${js.JSON.stringify(replaced)};"
  }

  def downloadDependencies(
      dependencies: Seq[Dependency],
      cacheDirectory: String
  ): Future[Map[Dependency, String]] =
    FetchDependencies
      .fetch(dependencies)
      .map(_.map {
        case (dependency, fileContents) =>
          val directory = path
            .join(cacheDirectory, dependency.module.organization.value, dependency.version, "jars")

          // TODO Support older versions of Node? The recursive option is only supported since 10.12.0
          fs.mkdirSync(directory, MakeDirectoryOptions(recursive = true))

          val fileName = s"${dependency.module.name.value}.jar"
          val filePath = path.join(directory, fileName)
          fs.writeFileSync(filePath, fileContents, "utf-8")

          (dependency -> filePath)
      })

}
