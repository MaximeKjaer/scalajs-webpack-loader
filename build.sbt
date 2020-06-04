lazy val root = (project in file("."))
  .enablePlugins(ScalaJSPlugin, ScalablyTypedConverterPlugin)
  .settings(
    name := "scalajs-webpack-loader",
    scalaVersion := "2.13.1",
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    stUseScalaJsDom := false,
    Compile / npmDependencies ++= Seq(
      "@types/loader-utils" -> "2.0.0",
      "@types/json-schema" -> "7.0.4",
      "@types/webpack" -> "4.41.7",
      "@types/node" -> "14.0.10",
      "@types/node-fetch" -> "2.5.7",
      "@types/fs-extra" -> "9.0.1",
      "schema-utils" -> "2.6.5"
    ),
    stIgnore ++= List(
      "ajv",
      "anymatch",
      "source-list-map",
      "tapable",
      "uglify-js",
      "webpack-sources"
    ),
    libraryDependencies ++= Seq(
      "io.get-coursier" %%% "coursier" % "2.0.0-RC6-21",
      "ch.epfl.scala" %%% "bloop-config" % "1.4.1-14-7af20d76",
      "org.scalameta" %%% "munit" % "0.7.3" % Test
    ),
    scalaSource in Test := baseDirectory.value / "test" / "unit",
    testFrameworks += new TestFramework("munit.Framework")
  )
