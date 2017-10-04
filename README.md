# Dais-Neon

Dais interceptor chain in Rust+Node.js with ClojureScript support

### Getting Started

You can compile the Rust and ClojureScript with `npm install`.

From there you can enter Node.js REPL with `node`:

```
$ node
> const dais = require('.')
> dais.hello()
'hello node - from Rust'
> dais.hellocljs()
'hello node - from ClojureScript'
```


### TODO

 * Find or write naive microbenching lib
 * Microbenchmarks of chain execution


