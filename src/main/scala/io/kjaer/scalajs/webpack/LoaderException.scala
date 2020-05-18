package io.kjaer.scalajs.webpack

import coursier.Dependency

sealed abstract class LoaderException(message: String) extends Exception(message)

sealed abstract class LoaderOptionsException(message: String) extends LoaderException(message)
case class OptionsValidationException(message: String)
    extends LoaderOptionsException(
      s"""The ${Loader.name} options do not match the schema. The following errors were found:
         |$message
         |""".stripMargin
    )
case class ScalaVersionParseException(version: String)
    extends LoaderOptionsException(
      s"""Could not parse the ${Loader.name} options field "options.scalaVersion"
         |  Expected a version string (e.g. "2.13.2")
         |  Got the string "$version"
         |""".stripMargin
    )
case class ScalaJSVersionParseException(version: String)
    extends LoaderOptionsException(
      s"""Could not parse the ${Loader.name} options field "options.scalaJSVersion"
         |  Expected a version string (e.g. "1.1.0")
         |  Got the string "$version"
         |""".stripMargin
    )
case class LibraryDependenciesParseException(parseErrors: Seq[String])
    extends LoaderOptionsException(
      s"""Could not parse the ${Loader.name} options field "options.libraryDependencies".
         |The following dependencies are invalid:
         |${parseErrors.mkString("  - ", "\n  - ", "\n")}
         |""".stripMargin
    )

case class FileReadException(file: String, message: String)
    extends LoaderException(s"Could not read file '$file': $message")

sealed abstract class DependencyFetchException(message: String) extends LoaderException(message)
case class ResolutionException(errors: Seq[(DependencyId, Seq[String])])
    extends DependencyFetchException(
      s"The following errors happened while resolving dependencies: ${errors.mkString("\n")}"
    )
case class DependencyConflictException(conflicts: Set[Dependency])
    extends DependencyFetchException(
      s"Conflicts were found in the following dependencies: ${conflicts.mkString("\n")}"
    )
case class DownloadException(errors: Seq[String])
    extends DependencyFetchException(
      s"An error happened while downloading artifacts:\n${errors.mkString("\n")}"
    )

sealed abstract class CompilationException(message: String) extends LoaderException(message)
case class CompilerException(stderr: String)
    extends CompilationException(s"Scala.js compilation failed with the following output:\n$stderr")
case class LinkerException(stderr: String)
    extends CompilationException(s"Scala.js linking failed with the following output:\n$stderr")
