import { assert } from "chai";
import { run } from "./runner";

describe("loader", () => {
  it("Inserts name and outputs JavaScript", async () => {
    const stats = await run("fixtures/example.txt");
    const output = stats.toJson().modules?.[0].source;
    assert.equal(output, 'export default "Hey Alice!\\n";');
  });
});
