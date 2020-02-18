# `scalajs-webpack`: Webpack loader for Scala.js

```console
$ npm install --save-dev scalajs-webpack
```

## Commands

| `npm run ...`        | Description                                                                      |
| -------------------- | -------------------------------------------------------------------------------- |
| `build`              | Run all build steps                                                              |
| `build:ts`           | Compile TypeScript files in `src` to `dist`                                      |
| `clean`              | Delete all build artifacts                                                       |
| `test`               | Run all tests                                                                    |
| `test:format`        | Test code formatting for all JavaScript, TypeScript, JSON and YAML files         |
| `test:lint`          | Test TypeScript files for linting errors                                         |
| `test:package`       | Test that paths in `package.json` exist                                          |
| `test:tslint-config` | Test that `tslint.json` does not contain rules conflicting with formatting rules |
| `test:unit`          | Run Mocha unit tests                                                             |
| `fix`                | Run all fixes                                                                    |
| `fix:lint`           | Fix linting errors in TypeScript files                                           |
| `fix:format`         | Fix formatting errors for all JavaScript, TypeScript, JSON and YAML files        |

## Releasing versions

To release a new version, run `npm version patch`, `npm version minor` or `npm version major`. Travis CI will automatically deploy the new version once the CI build passes.
