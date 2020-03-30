import * as webpack from "webpack";
import path from "path";

export const loader = path.resolve(__dirname, "../../../dist/bundle.js");

export const commonConfig: webpack.Configuration = {
  target: "node",
  mode: "development",
  output: {
    path: path.resolve(__dirname, "dist"),
    filename: "test-bundle.js"
  },
  module: {
    rules: [
      {
        test: /\.sjsproject$/,
        use: {
          loader
        }
      }
    ]
  }
};
