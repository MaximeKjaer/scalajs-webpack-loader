## Contributing

- Integration tests are done with Jest with `npm run test:integration`
- DevDependencies for integration tests go in `package.json`
- Runtime dependencies go in `build.sbt`. Some of them may be JS dependencies, and are managed by `scalajs-bundler`.
- Scala source code is formatted by [scalafmt](https://scalameta.org/scalafmt/) in sbt
- NPM source code is formatted by Prettier with npm
