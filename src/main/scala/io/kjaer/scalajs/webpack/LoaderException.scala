package io.kjaer.scalajs.webpack

import coursier.Dependency

sealed abstract class LoaderException(message: String) extends Exception(message)

case class OptionsValidationException(message: String)
    extends LoaderException(s"Options do not conform to schema.\n$message")

case class LibraryDependenciesParseException(parseErrors: Seq[String])
    extends LoaderException(
      s"Some libraryDependencies could not be parsed: ${parseErrors.mkString(", ")}"
    )

case class FileReadException(file: String, message: String)
    extends LoaderException(s"Could not read file '$file': $message")

sealed abstract class DependencyFetchException(message: String) extends LoaderException(message)

case class ResolutionException(errors: Seq[(DependencyName, Seq[String])])
    extends DependencyFetchException(
      s"Could not get metadata about the following dependencies: ${errors.mkString(", ")}"
    )
case class DependencyConflictException(conflicts: Set[Dependency])
    extends DependencyFetchException(
      s"Conflicts were found in the following dependencies: ${conflicts.mkString(", ")}"
    )
case class DownloadException(errors: Seq[String])
    extends DependencyFetchException(
      s"An error happened while downloading artifacts:\n${errors.mkString("\n")}"
    )

sealed abstract class CompilationException(message: String) extends LoaderException(message)

case class CompilerException(stderr: String)
    extends CompilationException(s"Compilation failed with the following output:\n${stderr}")
case class LinkerException(stderr: String)
    extends CompilationException(s"Linking failed with the following output:\n${stderr}")
