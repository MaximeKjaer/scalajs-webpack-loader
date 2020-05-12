package io.kjaer.scalajs.webpack

import coursier.Dependency

sealed abstract class LoaderException(message: String) extends Exception(message)

sealed abstract class LoaderOptionsException(message: String) extends LoaderException(message)
case class OptionsValidationException(message: String)
    extends LoaderOptionsException(s"Options do not conform to schema.\n$message")
case class ScalaVersionParseException(version: String)
    extends LoaderOptionsException(
      s"Could not parse scalaVersion '$version'. Expected a string of the format '2.13.2'"
    )
case class ScalaJSVersionParseException(version: String)
    extends LoaderOptionsException(
      s"Could not parse scalaJSVersion '$version'. Expected a string of the format '1.0.1'"
    )
case class LibraryDependenciesParseException(parseErrors: Seq[String])
    extends LoaderOptionsException(
      s"Some libraryDependencies could not be parsed: ${parseErrors.mkString("\n")}"
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
    extends CompilationException(s"Compilation failed with the following output:\n$stderr")
case class LinkerException(stderr: String)
    extends CompilationException(s"Linking failed with the following output:\n$stderr")
