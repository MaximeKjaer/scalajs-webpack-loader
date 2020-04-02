import { testError, testSnapshot } from "./testers";

describe("loader", function() {
  const networkTimeout = 20000;

  describe("example", () => {
    it("outputs the snapshot", async () => {
      await testSnapshot("example");
    }).timeout(networkTimeout);
  });

  describe("wrong-options", () => {
    it("fails with a schema validation error", async () => {
      await testError("wrong-options", "ValidationError");
    });
  });

  describe("scala-entry", () => {
    it("outputs the snapshot", async () => {
      await testSnapshot("scala-entry");
    }).timeout(networkTimeout);
  });

  describe("custom-scala-version", () => {
    it("outputs the snapshot", async () => {
      await testSnapshot("custom-scala-version");
    }).timeout(networkTimeout);
  });
});
