# Dais-Neon

Dais interceptor chain in Rust+Node.js with ClojureScript support.

There is also C++ support via node-gyp and [Native Abstractions for Node.js](https://github.com/nodejs/nan).


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


### TODO


