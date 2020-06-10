import * as webpack from "webpack";
import { commonConfig, loader } from "../webpack.common";
import path from "path";

const config: webpack.Configuration = {
  ...commonConfig,
  context: __dirname,
  entry: "./index.js",
  output: {
    path: path.resolve(__dirname, "dist"),
    filename: "scalajs-1.1.0-bundle.js"
  },
  module: {
    rules: [
      {
        test: /\.sjsproject$/,
        use: {
          loader,
          options: {
            scalaJSVersion: "1.1.0"
          }
        }
      }
    ]
  }
};

export default config;
