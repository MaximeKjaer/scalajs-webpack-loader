import path from "path";
import webpack from "webpack";
import MemoryFileSystem from "memory-fs";

export async function run(fixture: string): Promise<Buffer> {
  const { default: config } = await import(
    path.join(__dirname, "fixtures", fixture, "webpack.config.ts")
  );

  const compiler = webpack(config);
  const fs = new MemoryFileSystem();
  compiler.outputFileSystem = fs;

  return new Promise((resolve, reject) => {
    compiler.run((err, stats) => {
      const json = stats.toJson();
      const outputPath = json.outputPath;
      if (err) reject(err);
      else if (stats.hasErrors()) reject(new Error(json.errors.join("\n")));
      else if (outputPath === undefined) reject(new Error("No output path"));
      else
        resolve(fs.readFileSync(path.join(outputPath, config.output.filename)));
    });
  });
}
