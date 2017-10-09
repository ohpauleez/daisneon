
Dais-Neon
===========

Dais interceptor chain in Rust+Node.js with ClojureScript support.

There is also C++ support via node-gyp and [Native Abstractions for Node.js](https://github.com/nodejs/nan).

The Dais Interceptor Chain is a simple, synchronous-only interceptor chain,
inspired by [Pedestal](https://github.com/pedestal/pedestal).
[Dais](https://github.com/ohpauleez/dais) was first written in Java and
designed to be used in Java and Clojure.


### Getting Started

You can compile the Rust, C++, and ClojureScript with `npm install`.

From there you can enter Node.js REPL with `node`:

```
$ node
> const dais = require('.')
> dais.hello()
'hello node - from JavaScript'
> dais.hellorust()
'hello node - from Rust'
> dais.hellocljs()
'hello node - from ClojureScript'
> dais.hellocpp()
'hello node - from C++'
```

And you can also use the ClojureScript/Lumo REPL (without access to index.js)

```
$ make cljs-repl
cljs.user=> (require '[daisneon.bridge :as bridge])
nil
cljs.user=> (.hello bridge/rust)
"hello node - from Rust"
cljs.user=> (.Hello bridge/cpp)
"hello node - from C++"
```


### Hacking

 * This project uses `npm` for build tasks.
   For convenience, these are plumbed into a Makefile
 * The ClojureScript REPL has socketrepl support, all via Lumo
 * You may want to modify `target/main.js` and add `goog.require("daisneon.example");`
   if you're working at the node shell via index.js


### Basic benchmarks

To see the general numbers, modify `target/main.js` and add `goog.require("daisneon.example");`
Then you can examine chain executions:

```
$ node
> const dais = require('.')
> dais.testChains()
```

 * rust: 0.073ms
   * Static processing only (no dynamic chains, and no error handling)
 * cpp: 0.042ms
   * Static processing only (no dynamic chains, and no error handling)
 * js: 0.051ms
 * cljs-static: 0.287ms
 * cljs: 0.346ms
 * cljs-js-interop: 1.737ms
   * Involves converting JS objects to maps and then back to objects at the end

For reference, the Java Dais chain runs between 0.02ms - 0.20ms.
The Pedestal Chain runs between 1-3ms.


### TODO


