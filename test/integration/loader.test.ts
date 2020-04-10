import { testError, testSnapshot } from "./testers";

describe("loader", function() {
  const networkTimeout = 60 * 1000;

  describe("example", () => {
    it("outputs the snapshot", async () => {
      await testSnapshot("example");
    }).timeout(networkTimeout);
  });

  describe("wrong-options", () => {
    it("fails with a schema validation error", async () => {
      await testError("wrong-options", "Options do not conform to schema");
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

  describe("library-dependencies", () => {
    it("outputs the snapshot", async () => {
      await testSnapshot("library-dependencies");
    }).timeout(networkTimeout);
  });
});
