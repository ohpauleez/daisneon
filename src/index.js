'use strict'

// All of these require `npm install` to work
const addon = require('../native');
const cppaddon = require('../build/Release/cppaddon');
const daiscljs = require('../target/main.js');

function hello() {
  return 'hello node - from JavaScript';
}

function testChains() {
  console.time("rust");
  let rustRes = addon.executeChain({"dais.terminators": [ctx => ctx.b != undefined]},
                                              [{enter: ctx => {ctx.a = 1; return ctx},
                                                leave: ctx => {ctx.leaveA = 11; return ctx}},
                                               {enter: ctx => {ctx.b = 2; return ctx}},
                                               {enter: ctx => {ctx.c = 3; return ctx}}]);
  console.timeEnd("rust");
  console.time("cljs-interop");
  let cljsInterRes = daisneon.dais.execute({"dais.terminators": [ctx => cljs.core.get(ctx, "b")]},
                                                 [{enter: ctx => cljs.core.assoc(ctx, "a", 1),
                                                   leave: ctx => cljs.core.assoc(ctx, "leaveA", 11)},
                                                  {enter: ctx => cljs.core.assoc(ctx, "b", 2)},
                                                  {enter: ctx => cljs.core.assoc(ctx, "c", 3)}]);
  console.timeEnd("cljs-interop");
  console.time("cljs");
  let cljsRes = daisneon.example.exampleStatic1b();
  console.timeEnd("cljs");
  let res = {"rustResult": rustRes,
             "cljsInterResult": cljsInterRes,
             "cljsResult": cljs.core.clj__GT_js(cljsRes)};
  return res;
}

module.exports.hello = hello;
module.exports.hellorust = addon.hello;
module.exports.hellocljs = daisneon.dais.hello;
module.exports.hellocpp = cppaddon.Hello;

module.exports.executeChain = addon.executeChain;
module.exports.testChains = testChains;

