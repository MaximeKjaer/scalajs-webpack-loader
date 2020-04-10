# Changelog

## v0.0.3

- Add caching to Coursier resolutions

  The loader was previously caching artifacts, but not resolutions. Now, both are cached, meaning that an Internet connection is no longer required to build Scala.js code (unless the dependencies change).

  Note that this is a breaking change, as the internal paths to cached files have changed. They now match the paths to which Coursier saves files, which will enable sharing the cache with other Coursier clients (though this will be a feature for a future release).

- Fix `libraryDependencies` option to follow Mill format

  `libraryDependencies` now correctly follow Mill's format, with `:::` for Scala.js dependencies and `::` for Scala dependencies.

  The conversion of a dependency to a Maven artifact name has also been fixed.

- Clean up `targetDirectory` before compilation

  This fixes a problem where artifacts from previous builds were linked with the newly compiled artifacts, which would usually result in linking errors.

  Eventually, this will be superseded by proper incremental compilation. For now, compiling with a clean state will at least ensure correct compilation.

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
