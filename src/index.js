'use strict'

// All of these require `npm install` to work
const addon = require('../native');
const cppaddon = require('../build/Release/cppaddon');
const daiscljs = require('../target/main.js');

function hello() {
    return 'hello node - from JavaScript';
}

module.exports.hello = hello;
module.exports.hellorust = addon.hello;
module.exports.hellocljs = daisneon.dais.main;
module.exports.hellocpp = cppaddon.Hello;
module.exports.handleChain = addon.handleChain;

