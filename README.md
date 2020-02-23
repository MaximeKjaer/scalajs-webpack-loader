## Contributing

All development commands can be run with NPM:

| `npm run ...`       | Description                                             |
| ------------------- | ------------------------------------------------------- |
| `build`             | Run a full production build                             |
| `build:scalajs`     | Build Scala.js code in production mode                  |
| `build:bundle`      | Build the production Webpack bundle to `dist/bundle.js` |
| `start`             | Start file watchers to compile in development mode      |
| `start:scalajs`     | Start Scala.js fastOpt file watching                    |
| `start:bundle`      | Start Webpack file watching in development mode         |
| `test`              | Run all tests                                           |
| `test:package`      | Test that `package.json` is valid                       |
| `test:format:scala` | Test formatting of Scala files with scalafmt            |
| `test:format:js`    | Test formatting of JS and config files                  |
| `test:integration`  | Run integration tests                                   |
| `fix`               | Run all automatic fixes                                 |
| `fix:format:scala`  | Run scalafmt fixes for all Scala files                  |
| `fix:format:js`     | Run Prettier fixes for all JS and config files          |
| `clean`             | Delete all build artifacts                              |
| `clean:scala`       | Delete Scala build artifacts                            |
| `clean:js`          | Delete Webpack build artifacts                          |

Scalafmt commands try to run the CLI, and fall back to using SBT. If you run these commands frequently, you may want to install the [scalafmt CLI](https://scalameta.org/scalafmt/docs/installation.html#cli) for faster execution of these commands (`coursier install scalafmt` if you have Coursier installed).
