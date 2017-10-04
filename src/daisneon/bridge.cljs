(ns daisneon.bridge)

(def rust (js/require "./native/index.node"))
(def cpp (js/require "./build/Release/cppaddon.node"))

