import * as webpack from "webpack";
import { commonConfig } from "../webpack.common";

const config: webpack.Configuration = {
  ...commonConfig,
  context: __dirname,
  entry: "./example.txt"
};

export default config;
