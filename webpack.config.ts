import * as path from "path";
import * as webpack from "webpack";

const mode: "production" | "development" =
  process.env.NODE_ENV == "production" ? "production" : "development";

const scalaJSBundle = path.resolve(
  __dirname,
  `target/scala-2.13/scalajs-webpack-loader-${
    mode === "production" ? "opt" : "fastopt"
  }.js`
);

const config: webpack.Configuration = {
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

export default config;
