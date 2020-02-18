import { hello } from "../src/hello";

describe("hello()", () => {
  it("says 'hello'", () => {
    expect(hello()).toBe("hello world");
  });
});
