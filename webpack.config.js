const path = require("path");
const { dependencies } = require("./package.json");

module.exports = {
  mode: "development",
  entry: "./src/main/js/index.js",
  output: {
    path: path.resolve(__dirname, "dist"),
    filename: "bundle.js",
    libraryTarget: "commonjs"
  },
  resolve: {
    modules: [path.resolve(__dirname, "target", "scala-2.13")]
  },
  externals: Object.keys(dependencies)
};
