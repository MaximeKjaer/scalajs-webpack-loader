import * as webpack from "webpack";
import { commonConfig, loader } from "../webpack.common";
import path from "path";

const config: webpack.Configuration = {
  ...commonConfig,
  context: __dirname,
  entry: "./index.js",
  output: {
    path: path.resolve(__dirname, "dist"),
    filename: "scalac-options.js"
  },
  module: {
    rules: [
      {
        test: /\.sjsproject$/,
        use: {
          loader,
          options: {
            scalacOptions: ["-language:postfixOps"]
          }
        }
      }
    ]
  }
};

export default config;
