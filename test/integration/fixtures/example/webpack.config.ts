import * as webpack from "webpack";
import { commonConfig } from "../webpack.common";
import path from "path";

const config: webpack.Configuration = {
  ...commonConfig,
  context: __dirname,
  entry: "./index.js",
  output: {
    path: path.resolve(__dirname, "dist"),
    filename: "example-bundle.js"
  }
};

export default config;
