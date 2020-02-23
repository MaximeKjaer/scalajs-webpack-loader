const merge = require("webpack-merge");
const common = require("./webpack.common.js");
const path = require("path");

module.exports = merge(common, {
  mode: "development",
  resolve: {
    alias: {
      "scalajs-webpack-loader": path.resolve(
        __dirname,
        "target/scala-2.13/scalajs-webpack-loader-fastopt.js"
      )
    }
  }
});
