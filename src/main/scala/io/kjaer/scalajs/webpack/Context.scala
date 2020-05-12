package io.kjaer.scalajs.webpack

import typings.webpack.mod.loader.LoaderContext

case class Context(
    loader: LoaderContext,
    options: ParsedOptions,
    logger: LoaderLogger
)
