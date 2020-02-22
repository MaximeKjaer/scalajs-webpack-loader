enablePlugins(ScalaJSPlugin)

name := "scalajs-webpack-loader"
scalaVersion := "2.13.1"

scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
