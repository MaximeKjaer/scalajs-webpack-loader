const path = require("path");
const common = require("./jest.common");

module.exports = {
  ...common,
  globals: {
    __BUNDLE__: path.resolve("target/scala-2.13/scalajs-webpack-loader-opt.js")
  }
};
