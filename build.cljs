(require '[lumo.build.api :as b])

(def adv-build {:main 'daisneon.dais
                :output-dir "target"
                :output-to "target/main.js"
                :source-map "target/main.js.map" ;; Does not currently work in Lumo -- https://github.com/anmonteiro/lumo/issues/132
                :optimizations :advanced
                :target :nodejs
                ;:cache-analysis true ;; defaults to `true` iff optimizations is :none
                :pretty-print false
                :static-fns true
                :fn-invoke-direct true
                ;:install-deps :true
                ;:npm-deps []
                })

(def dev-build {:main 'daisneon.dais
                :output-dir "target"
                :output-to "target/main.js"
                :optimizations :none
                :source-map true
                :target :nodejs
                ;:cache-analysis true ;; defaults to `true` iff optimizations is :none
                ;:pretty-print true
                ;:static-fns true
                ;:fn-invoke-direct true
                ;:install-deps :true
                ;:npm-deps []
                })
;; Prod build
(b/build "src" dev-build)

