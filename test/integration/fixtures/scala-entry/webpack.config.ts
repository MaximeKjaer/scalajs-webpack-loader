import * as webpack from "webpack";
import { commonConfig, loader } from "../webpack.common";
import path from "path";

const config: webpack.Configuration = {
    ...commonConfig,
    context: __dirname,
    entry: "./.sjsproject",
    output: {
        path: path.resolve(__dirname, "dist"),
        filename: "scala-entry.js"
    },
    module: {
        rules: [
            {
                test: /\.sjsproject$/,
                use: {
                    loader,
                    options: {
                        mainMethod: "Main.main"
                    }
                }
            }
        ]
    }
};

export default config;
