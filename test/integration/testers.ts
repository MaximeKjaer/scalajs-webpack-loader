import * as path from "path";
import { run } from "./runner";
import * as fs from "fs";
import * as chai from "chai";
import { assert } from "chai";
import chaiAsPromised = require("chai-as-promised");

chai.use(chaiAsPromised);
const expect = chai.expect;

export async function testSnapshot(fixture: string): Promise<void> {
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

export async function testError(
  fixture: string,
  expectedError: string
): Promise<void> {
  return expect(run("wrong-options")).to.eventually.rejectedWith(
    "ValidationError"
  );
}
