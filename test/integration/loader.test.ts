import { testError, testSnapshot } from "./testers";

describe("loader", function() {
  this.timeout(20000);

  describe("example", () => {
    it("outputs the snapshot", async () => {
      await testSnapshot("example");
    });
  });

  describe("wrong-options", () => {
    it("fails with a schema validation error", async () => {
      await testError("wrong-options", "ValidationError");
    });
  });

  describe("scala-entry", () => {
    it("outputs the snapshot", async () => {
      await testSnapshot("scala-entry");
    });
  });
});
