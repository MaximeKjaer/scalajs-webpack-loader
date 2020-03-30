<div align="center">

# scalajs-webpack-loader

<a href="https://github.com/webpack/webpack">
    <img alt="Webpack logo" src="https://cdn.rawgit.com/webpack/media/e7485eb2/logo/icon.svg" height="150"/>
</a>
<a href="https://github.com/scala-js/scala-js">
    <img alt="Scala.js logo" src="http://www.scala-js.org/assets/img/scala-js-logo.svg" height="150"/>
</a>

Plugin and Loader for [webpack](http://webpack.js.org/) and [Scala.js](https://www.scala-js.org/).
Import Scala.js code in a Webpack project, without having to set up a Scala build.

</div>

## Installing

scalajs-webpack-loader currently requires at least Node v10.0. Check your version using:

```console
$ node -v
```

## Development

To develop the code in this repo, you will need to have `sbt` and `npm` installed on your system. Run `npm install` after cloning the repo to get all JS dependencies of this project.

All development commands can be run with NPM. Note that some of these commands depend on the `NODE_ENV` environment variable, which can either be set to `production` or `development`; if it isn't set, it's interpreted as `development`. Depending on the environment variable, some commands will execute in development (fast) mode or in production (slow, optimized) mode.

| `npm run ...`               | Description                                                           |
| --------------------------- | --------------------------------------------------------------------- |
| `build`                     | Run a full build                                                      |
| `build:scalajs`             | Build Scala.js sources in mode dictated by `NODE_ENV`                 |
| `build:scalajs:production`  | Build Scala.js sources in production mode (`fullOptJS`)               |
| `build:scalajs:development` | Build Scala.js sources in development mode (`fastOptJS`)              |
| `build:bundle`              | Build final output bundle  in mode dictated by `NODE_ENV`             |
| `test`                      | Run all tests                                                         |
| `test:package`              | Test that `package.json` is valid                                     |
| `test:format:scala`         | Test formatting of Scala files with scalafmt                          |
| `test:format:js`            | Test formatting of JS and config files                                |
| `test:integration`          | Run integration tests                                                 |
| `fix`                       | Run all automatic fixes                                               |
| `fix:format:scala`          | Run scalafmt fixes for all Scala files                                |
| `fix:format:js`             | Run Prettier fixes for all JS and config files                        |
| `clean`                     | Delete all build artifacts                                            |
| `clean:scalajs`             | Delete Scala.js build artifacts                                       |
| `clean:bundle`              | Clean Webpack's `dist/bundle.js`                                      |
| `clean:fixtures`            | Clean local caches and build artifacts from integration test fixtures |

Remember to build before testing.

Scalafmt commands try to run the CLI, and fall back to using SBT. If you run these commands frequently, you may want to install the [scalafmt CLI](https://scalameta.org/scalafmt/docs/installation.html#cli) for faster execution of these commands (`coursier install scalafmt` if you have [Coursier](https://get-coursier.io/) installed on your `PATH`).
