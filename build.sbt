enablePlugins(ScalaJSPlugin)
enablePlugins(ScalaJSBundlerPlugin)

name := "scalajs-webpack-loader"
scalaVersion := "2.13.1"

npmDependencies in Compile ++= Seq(
  "loader-utils" -> "^1.4.0"
)
