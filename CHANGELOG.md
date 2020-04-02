# Changelog

## v0.0.2

- Add `scalaVersion` and `scalaJSVersion` options

  Previously, the plugin only worked with Scala.js 1.0.0 and Scala 2.13.1. You can now specify which version you want to use.

- Add `libraryDependencies` option

  With this new option, you can specify a list of Scala.js dependencies that Scala.js projects should be compiled with. For example:

  ```javascript
  {
    options: {
      libraryDependencies: ["com.lihaoyi::upickle:0.9.9"];
    }
  }
  ```

- Improve default logging output

  This ensures that relevant information is logged on default settings. Previously, the logger was perhaps too silent, which meant that on the first run, it would seem like nothing was happening.

## v0.0.1

Initial release 🎉
