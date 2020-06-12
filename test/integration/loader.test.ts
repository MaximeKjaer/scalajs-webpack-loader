import { testError, testSnapshot } from "./testers";

describe("loader", function() {
  const networkTimeout = 60 * 1000;

  describe("example", () => {
    it("outputs the snapshot", async () => {
      await testSnapshot("example");
    }).timeout(networkTimeout);
  });

  describe("imports", () => {
    it("outputs the snapshot", async () => {
      await testSnapshot("imports");
    }).timeout(networkTimeout);
  });

  describe("wrong-options", () => {
    it("fails with a schema validation error", async () => {
      await testError(
        "wrong-options",
        "Error: The scalajs-webpack-loader options do not match the schema. The following errors were found:\n" +
          "ValidationError: Invalid configuration object. scalajs-webpack-loader has been initialized using a configuration object that does not match the API schema.\n" +
          " - configuration has an unknown property 'thisShouldNotBeAccepted'. These properties are valid:"
      );
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

  // TODO re-enable this test once bloop#1308 has been closed
  // describe("scalajs-1.1.0", () => {
  //   it("outputs the snapshot", async () => {
  //     await testSnapshot("scalajs-1.1.0");
  //   }).timeout(networkTimeout);
  // });

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
    }).timeout(networkTimeout);
  });

  describe("wrong-scala-version", () => {
    it("fails with a parsing error", async () => {
      await testError(
        "wrong-scala-version",
        'Could not parse the scalajs-webpack-loader options field "options.scalaVersion"\n' +
          '  Expected a version string (e.g. "2.13.2")\n' +
          '  Got the string "this is obviously wrong"\n'
      );
    });
  });

  describe("wrong-scalajs-version", () => {
    it("fails with a parsing error", async () => {
      await testError(
        "wrong-scalajs-version",
        'Could not parse the scalajs-webpack-loader options field "options.scalaJSVersion"\n' +
          '  Expected a version string (e.g. "1.1.0")\n' +
          '  Got the string "this is obviously wrong"\n'
      );
    });
  });
});
