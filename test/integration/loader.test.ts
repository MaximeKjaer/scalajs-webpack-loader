import { assert } from "chai";
import { run } from "./runner";

describe("loader", function() {
  this.timeout(20000);

  it("Inserts name and outputs JavaScript", async () => {
    const stats = await run("example");
    const output = stats.toJson().modules?.[0].source;
    assert.equal(output, 'export default "Hey Alice!\\n";');
  });
});
