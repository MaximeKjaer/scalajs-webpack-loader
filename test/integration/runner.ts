import path from "path";
import webpack from "webpack";
import MemoryFileSystem from "memory-fs";

type Verbosity = "silent" | "error" | "warn" | "info" | "debug" | "trace";

export async function run(
  fixture: string,
  verbosity: Verbosity | undefined = undefined
): Promise<Buffer> {
  let { default: config } = (await import(
    path.join(__dirname, "fixtures", fixture, "webpack.config.ts")
  )) as { default: webpack.Configuration };

  // Override some of the loader options.
  //
  // The fixtures are meant to be able to run both as a Mocha test, and as a standalone project that
  // can be built with `npx webpack`. In Mocha mode we want to override some settings:
  //
  //   - Verbosity is what we set the test to be. This allows us to debug a single test, or to
  //     manage verbosity in all tests at once.
  //   - For most scalajs-webpack-loader projects, "target" is a good default for the target
  //     directory. However, for this project itself, when building fixtures from the root folder in
  //     Mocha tests, "target" is already used by the sbt build. Artifacts from the fixtures should
  //     not be mixed in with the project's own build artifacts.
  const loaderOptions = getLoaderOptions(config);
  if (verbosity) loaderOptions.verbosity = verbosity;
  loaderOptions.targetDirectory = "test-target";

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
        resolve(
          fs.readFileSync(
            path.join(outputPath, config.output!.filename as string)
          )
        );
    });
  });
}

function getLoaderOptions(config: webpack.Configuration): { [k: string]: any } {
  const loaderConfig = config.module?.rules.find(rule => {
    return rule.test instanceof RegExp && rule.test.test(".sjsproject");
  })?.use;
  if (typeof loaderConfig !== "object" || Array.isArray(loaderConfig)) {
    throw new Error("Could not find a 'use' object in the Webpack config");
  }
  if (typeof loaderConfig.options === "string") {
    throw new Error(
      "String-type loader config is not supported by the integration test framework"
    );
  }
  if (loaderConfig.options === undefined) {
    loaderConfig.options = {};
  }
  return loaderConfig.options;
}
