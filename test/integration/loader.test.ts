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

  describe("scalac-options", () => {
    it("outputs the snapshot", async () => {
      await testSnapshot("scalac-options");
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

  describe("wrong-dependencies", () => {
    it("fails with a 404 error", async () => {
      await testError(
        "wrong-dependencies",
        "Error downloading ThisOrgDoesNotExist:ThisPackageDoesNotExist_sjs1_2.13:1.0.0\n" +
          "  Server replied with HTTP 404"
      );
    });
  });

  describe("wrong-scala-version", () => {
    it("fails with a parsing error", async () => {
      await testError("wrong-scala-version", "Could not parse scalaVersion");
    });
  });

  describe("wrong-scalajs-version", () => {
    it("fails with a parsing error", async () => {
      await testError(
        "wrong-scalajs-version",
        "Could not parse scalaJSVersion"
      );
    });
  });
});
