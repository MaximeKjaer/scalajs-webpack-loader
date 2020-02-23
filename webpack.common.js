const path = require("path");
const { dependencies } = require("./package.json");

module.exports = {
  entry: "./src/main/js/index.js",
  output: {
    path: path.resolve(__dirname, "dist"),
    filename: "bundle.js",
    libraryTarget: "commonjs"
  },
  externals: Object.keys(dependencies)
};
