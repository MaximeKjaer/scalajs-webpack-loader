package io.kjaer.scalajs.webpack

import coursier.util.EitherT
import typings.node.{Buffer, nodeStrings, childProcessMod => childProcess}
import typings.fsExtra.{mod => fs}

import scala.scalajs.js
import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

object NodeUtils {
  def execJava(
      classpath: Seq[String],
      mainClass: String,
      options: Seq[String]
  ): EitherT[Future, String, String] =
    execCommand("java", Seq("-cp", classpath.mkString(":"), mainClass) ++ options)

  /**
    * Execute a command-line command asynchronously
    * @param command Name of the command
    * @param options Options that will be passed space-separated to `command`
    * @return EitherT of Future containing either Left of stderr, or Right of stdout
    */
  def execCommand(
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

  def readFile(path: String): EitherT[Future, LoaderException, Buffer] = {
    EitherT(
      fs.readFile(path)
        .toFuture
        .map(Right(_))
        .recover(err => Left(FileReadException(path, err.getMessage)))
    )
  }

  def writeFile(
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
