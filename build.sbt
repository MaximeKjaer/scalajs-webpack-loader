enablePlugins(ScalaJSPlugin)

name := "scalajs-webpack-loader"
scalaVersion := "2.13.1"

resolvers += Resolver.bintrayRepo("oyvindberg", "ScalablyTyped")
libraryDependencies ++= Seq(
  "org.scalablytyped" %%% "loader-utils" % "1.1-dt-20180306Z-20260d",
  "org.scalablytyped" %%% "webpack" % "4.41-dt-20200209Z-7b3b5d"
)

scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
