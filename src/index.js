'use strict'

// All of these require `npm install` to work
const addon = require('../native');
const cppaddon = require('../build/Release/cppaddon');
const daiscljs = require('../target/main.js');
const daisjs = require('./dais.js');

function hello() {
  return 'hello node - from JavaScript';
}

function testChains() {
  console.time("rust");
  let rustRes = addon.basicExecute({"dais.terminators": [ctx => ctx.b !== undefined]},
                                   [{enter: ctx => {ctx.a = 1; return ctx},
                                     leave: ctx => {ctx["leave-a"] = 11; return ctx}},
                                    {enter: ctx => {ctx.b = 2; return ctx}},
                                    {enter: ctx => {ctx.c = 3; return ctx}}]);
  console.timeEnd("rust");
  console.time("cpp");
  let cppRes = cppaddon.BasicExecute({"dais.terminators": [ctx => ctx.b !== undefined]},
                                   [{enter: ctx => {ctx.a = 1; return ctx},
                                     leave: ctx => {ctx["leave-a"] = 11; return ctx}},
                                    {enter: ctx => {ctx.b = 2; return ctx}},
                                    {enter: ctx => {ctx.c = 3; return ctx}}]);
  console.timeEnd("cpp");
  console.time("js");
  let jsRes = daisjs.execute({"dais.terminators": [ctx => ctx.b !== undefined]},
                                   [{enter: ctx => {ctx.a = 1; return ctx},
                                     leave: ctx => {ctx["leave-a"] = 11; return ctx}},
                                    {enter: ctx => {ctx.b = 2; return ctx}},
                                    {enter: ctx => {ctx.c = 3; return ctx}}]);
  console.timeEnd("js");
  console.time("cljs-js-interop");
  let cljsInterRes = daisneon.dais.execute({"dais.terminators": [ctx => cljs.core.get(ctx, "b")]},
                                           [{enter: ctx => cljs.core.assoc(ctx, "a", 1),
                                             leave: ctx => cljs.core.assoc(ctx, "leave-a", 11)},
                                            {enter: ctx => cljs.core.assoc(ctx, "b", 2)},
                                            {enter: ctx => cljs.core.assoc(ctx, "c", 3)}]);
  console.timeEnd("cljs-js-interop");
  console.time("cljs-static");
  let cljsStaticRes = daisneon.example.exampleStatic1b();
  console.timeEnd("cljs-static");
  console.time("cljs");
  let cljsRes = daisneon.example.example1b();
  console.timeEnd("cljs");
  let res = {"rustResult": rustRes,
             "cppResult": cppRes,
             "jsResult": jsRes,
             "cljsInterResult": cljsInterRes,
             "cljsStaticResult": cljs.core.clj__GT_js(cljsStaticRes),
             "cljsResult": cljs.core.clj__GT_js(cljsRes),
  };
  return res;
}

function testA (x) {
    return x+1;
}

const testB = (x) => {return x+1};

function testFns() {
    let x = 1;
    console.time("funct");
    testA(x);
    console.timeEnd("funct");
    console.time("const");
    testB(x);
    console.timeEnd("const");
    console.time("funct2");
    for (let i = 0; i < 10000; i++) {
        testA(x);
    }
    console.timeEnd("funct2");
    console.time("const2");
    for (let i = 0; i < 10000; i++) {
        testB(x);
    }
    console.timeEnd("const2");
}

module.exports.hello = hello;
module.exports.hellorust = addon.hello;
module.exports.hellocljs = daisneon.dais.hello;
module.exports.hellocpp = cppaddon.Hello;

module.exports.executeChain = addon.executeChain;
module.exports.testChains = testChains;

module.exports.testFns = testFns;

