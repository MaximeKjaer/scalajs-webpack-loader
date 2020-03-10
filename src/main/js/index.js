import loader from "scalajs-bundle";

/**
 * In Node.js, `global.require` is sometimes undefined, even when `require` is not. See:
 * 
 *   https://stackoverflow.com/q/34566343/918389
 * 
 * Coursier uses Scala.js 0.6.x. To access the `require` function, it goes through Scala.js'
 * `js.Dynamic.global`, which is a window on the JavaScript `global` object. The way Webpack
 * loaders are loaded, `global.require` is undefined, so when Coursier fetches dependencies
 * through `global.require(...)`, a type error is thrown.
 * 
 * We therefore need to patch this, by setting `global.require = require`. However, we do not have
 * access to `require` in Scala.js 0.6.x; that's what landed us in this mess in the first place.
 * Perhaps we can run an `eval("global.require = require")` from Scala.js?
 * 
 * It turns out that this is impossible. A Scala.js `js.eval` is compiled to `globa["eval"](...)`.
 * JavaScript has a distinction between direct calls to `eval` (like `eval('1 + 1')`) and indirect
 * calls to eval (like `const e = eval; e('1 + 1')`). The indirect calls to eval are evaluated
 * in the global scope, while direct calls are evaluated in the local scope. See:
 * 
 *    https://stackoverflow.com/a/30140906/918389
 * 
 * This means that `require` is not available through eval in Scala.js 0.6.x either.
 * 
 * Setting `global.require` must thus be done in JavaScript, bundled with the Scala.js through
 * Webpack. However, the JS code cannot simply be `global.require = require`. This would break
 * Webpack's static analysis of imports; it complains with:
 * 
 *    Critical dependency: require function is used in a way in which dependencies cannot be
 *    statically extracted
 * 
 * To trick Webpack into not caring about this, this is therefore inside of an `eval`.
 */
eval("global.require = require");

export default loader;
