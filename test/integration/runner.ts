import path from "path";
import webpack from "webpack";
import MemoryFileSystem from "memory-fs";

export async function run(fixture: string): Promise<webpack.Stats> {
  const { default: config } = await import(
    path.join(__dirname, "fixtures", fixture, "webpack.config.ts")
  );

  const compiler = webpack(config);
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
