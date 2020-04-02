<div align="center">

# scalajs-webpack-loader

<a href="https://github.com/webpack/webpack">
    <img alt="Webpack logo" src="https://cdn.rawgit.com/webpack/media/e7485eb2/logo/icon.svg" height="150"/>
</a>
<a href="https://github.com/scala-js/scala-js">
    <img alt="Scala.js logo" src="http://www.scala-js.org/assets/img/scala-js-logo.svg" height="150"/>
</a>

[Webpack](http://webpack.js.org/) loader for [Scala.js](https://www.scala-js.org/).  
Import Scala.js code in a Webpack project, without having to set up a separate Scala build.

</div>

[![Build Status](https://travis-ci.com/MaximeKjaer/scalajs-webpack-loader.svg?branch=master)](https://travis-ci.com/MaximeKjaer/scalajs-webpack-loader)
[![npm version](https://img.shields.io/npm/v/scalajs-webpack-loader)](https://www.npmjs.com/package/scalajs-webpack-loader)

scalajs-webpack-loader is similar to [scalajs-bundler](https://github.com/scalacenter/scalajs-bundler) in that it is a tool that uses Webpack to bundle Scala.js code alongside JavaScript code. Use scalajs-bundler if your build tool is SBT, and use scalajs-webpack-loader if your build tool is Webpack.

## Installing

Install scalajs-webpack-loader as a dev-dependency of your NPM package by running:

```console
$ npm install --save-dev scalajs-webpack-loader
```

scalajs-webpack-loader currently requires at least Node v10.0. Check your version using:

```console
$ node -v
```

## Usage

### Basic usage

This example assumes the following directory structure:

```
├── src/
|   ├── js/
|   |   └── index.js
|   └── scala/
|       ├── .sjsproject
|       └── HelloWorld.scala
├── webpack.config.js
└── package.json
```

scalajs-webpack-loader allows you to import a Scala.js module into another Webpack module. A Scala.js module is a directory containing `*.scala` files, with an empty `.sjsproject` file at the root.

You can then import the Scala.js module in `index.js` as follows:

```javascript
import { HelloWorld } from "../scala/.sjsproject";

HelloWorld.sayHello();
```

You should add the loader to your `webpack.config.js`:

```javascript
module.exports = {
  // ...
  module: {
    rules: [
      {
        test: /\.sjsproject$/,
        use: [
          {
            loader: "scalajs-webpack-loader",
            options: {
              // verbosity: "warn",
              // mainMethod: "Main.main",
              // etc. See "Loader options" below
            }
          }
        ]
      }
    ]
  }
};
```

### Loader options

| Option key        | Type                                                              | Default     | Description                                                   |
| ----------------- | ----------------------------------------------------------------- | ----------- | ------------------------------------------------------------- |
| `mainMethod`      | string or undefined                                               | `undefined` | Execute the specified `main(Array[String])` method on startup |
| `verbosity`       | `"trace"`, `"debug"`, `"info"`, `"warn"`, `"error"` or `"silent"` | `"info"`    | Do not display log levels below specified verbosity           |
| `targetDirectory` | string                                                            | `"target"`  | Target directory for intermediary Scala build artifacts       |
| `scalaVersion`    | string                                                            | `"2.13.1"`  | Version of the Scala compiler                                 |
| `scalaJSVersion`  | string                                                            | `"1.0.0"`   | Version of the Scala.js compiler                              |

## Development

To develop the code in this repo, you will need to have `sbt` and `npm` installed on your system. Run `npm install` after cloning the repo to get all JS dependencies of this project.

All development commands can be run with NPM. Note that some of these commands depend on the `NODE_ENV` environment variable, which can either be set to `production` or `development`; if it isn't set, it's interpreted as `development`. Depending on the environment variable, some commands will execute in development (fast) mode or in production (slow, optimized) mode.

| `npm run ...`               | Description                                                           |
| --------------------------- | --------------------------------------------------------------------- |
| `build`                     | Run a full build                                                      |
| `build:scalajs`             | Build Scala.js sources in mode dictated by `NODE_ENV`                 |
| `build:scalajs:production`  | Build Scala.js sources in production mode (`fullOptJS`)               |
| `build:scalajs:development` | Build Scala.js sources in development mode (`fastOptJS`)              |
| `build:bundle`              | Build final output bundle in mode dictated by `NODE_ENV`              |
| `test`                      | Run all tests                                                         |
| `test:package`              | Test that `package.json` is valid                                     |
| `test:format:scala`         | Test formatting of Scala files with scalafmt                          |
| `test:format:js`            | Test formatting of JS and config files                                |
| `test:integration`          | Run integration tests                                                 |
| `test:unit`                 | Run Scala.js unit tests                                               |
| `fix`                       | Run all automatic fixes                                               |
| `fix:format:scala`          | Run scalafmt fixes for all Scala files                                |
| `fix:format:js`             | Run Prettier fixes for all JS and config files                        |
| `clean`                     | Delete all build artifacts                                            |
| `clean:scalajs`             | Delete Scala.js build artifacts                                       |
| `clean:bundle`              | Clean Webpack's `dist/bundle.js`                                      |
| `clean:fixtures`            | Clean local caches and build artifacts from integration test fixtures |

Remember to build before testing.

Scalafmt commands try to run the CLI, and fall back to using SBT. If you run these commands frequently, you may want to install the [scalafmt CLI](https://scalameta.org/scalafmt/docs/installation.html#cli) for faster execution of these commands (`coursier install scalafmt` if you have [Coursier](https://get-coursier.io/) installed on your `PATH`).

To release a new version, run `npm version patch`, `npm version minor` or `npm version major`. Travis CI will automatically deploy the new version once the CI build passes.
