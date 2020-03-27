import * as fs from "fs";
import { assert } from "chai";
import { run } from "./runner";
import * as path from "path";

describe("loader", function() {
  this.timeout(20000);

  async function testSnapshot(fixture: string): Promise<void> {
    const expectedFile = path.join(__dirname, "snapshots", fixture + ".js");
    const actualDumpFile = path.join(
      __dirname,
      "snapshots",
      fixture + ".actual.js"
    );
    const [actual, expected] = await Promise.all([
      run(fixture),
      fs.promises.readFile(expectedFile)
    ]);
    const pass = actual.equals(expected);
    if (!pass) {
      fs.writeFileSync(actualDumpFile, actual);
    }
    assert(
      pass,
      `Actual output did not equal. Dumped actual results to ${actualDumpFile}\n` +
        "     To override the expected output with the actual, run:\n" +
        `       mv ${actualDumpFile} ${expectedFile}`
    );
  }

  describe("example", () => {
    it("outputs the snapshot", async () => {
      await testSnapshot("example");
    });
  });
});
