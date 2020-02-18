import { assert } from "chai";
import { hello } from "../src/hello";

describe("hello", () => {
  it("says hello", () => {
    assert.strictEqual(hello(), "hello world");
  });
});
