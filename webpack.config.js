const path = require("path");

const mode =
  process.env.NODE_ENV == "production" ? "production" : "development";

const scalaJSBundle = path.resolve(
  __dirname,
  `target/scala-2.13/scalajs-webpack-loader-${
    mode === "production" ? "opt" : "fastopt"
  }.js`
);

module.exports = {
  entry: "./src/main/js/index.js",
  mode,
  target: "node",
  output: {
    path: path.resolve(__dirname, "dist"),
    filename: "bundle.js",
    library: "scalajs-webpack-loader",
    libraryTarget: "umd",
    libraryExport: "default"
  },
  resolve: {
    alias: {
      "scalajs-bundle": scalaJSBundle
    }
  }
};
