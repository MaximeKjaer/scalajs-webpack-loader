import path from "path";
import webpack from "webpack";
import MemoryFileSystem from "memory-fs";

const mode: "production" | "development" =
  process.env.NODE_ENV == "production" ? "production" : "development";

const loader = path.resolve(__dirname, "../../dist/bundle.js");

export function run(fixture: string): Promise<webpack.Stats> {
  const compiler = webpack({
    mode,
    context: __dirname,
    entry: `./${fixture}`,
    output: {
      path: path.resolve(__dirname),
      filename: "test-bundle.js"
    },
    target: "node",
    module: {
      rules: [
        {
          test: /\.txt$/,
          use: {
            loader,
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
      if (stats.hasErrors())
        reject(new Error(stats.toJson().errors.join("\n")));

      resolve(stats);
    });
  });
}
