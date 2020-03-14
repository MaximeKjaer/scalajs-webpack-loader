import path from "path";
import webpack from "webpack";
import MemoryFileSystem from "memory-fs";

const mode: "production" | "development" =
  process.env.NODE_ENV == "production" ? "production" : "development";

const loader = path.resolve(__dirname, "../../dist/bundle.js");

export async function run(fixture: string): Promise<webpack.Stats> {
  const fixtureDirectory = path.join(__dirname, "fixtures", fixture);
  const { default: config } = await import(
    path.join(fixtureDirectory, "webpack.config.ts")
  );

  const compiler = webpack({
    mode,
    context: fixtureDirectory,
    output: {
      path: "/",
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
    },
    ...config
  });

  compiler.outputFileSystem = new MemoryFileSystem();

  return new Promise((resolve, reject) => {
    compiler.run((err, stats) => {
      if (err) reject(err);
      else if (stats.hasErrors())
        reject(new Error(stats.toJson().errors.join("\n")));
      else resolve(stats);
    });
  });
}
