import path from "path";
import webpack from "webpack";
import MemoryFileSystem from "memory-fs";

export default (fixture, options = {}) => {
  const compiler = webpack({
    context: __dirname,
    entry: `./${fixture}`,
    output: {
      path: path.resolve(__dirname),
      filename: "test-bundle.js"
    },
    module: {
      rules: [
        {
          test: /\.txt$/,
          use: {
            loader: path.resolve(__dirname, "../../dist/bundle.js"),
            options: {
              name: "Alice"
            }
          }
        }
      ]
    }
  });

  compiler.outputFileSystem = new MemoryFileSystem();

  return new Promise((resolve, reject) => {
    compiler.run((err, stats) => {
      if (err) reject(err);
      if (stats?.hasErrors()) reject(new Error(stats.toJson().errors));

      resolve(stats);
    });
  });
};
