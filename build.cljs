(require '[lumo.build.api :as b])

(def builds {;; Prod Build
             ;; ----------------
             "prod" {:main 'daisneon.dais
                :output-dir "target"
                :output-to "target/main.js"
                :source-map "target/main.js.map" ;; Does not currently work in Lumo -- https://github.com/anmonteiro/lumo/issues/132
                :optimizations :advanced
                :target :nodejs
                ;:cache-analysis true ;; defaults to `true` iff optimizations is :none
                :pretty-print false
                :static-fns true
                :fn-invoke-direct true
                ;:foreign-libs [{:file "src"
                ;                :provides ["dais"]}]
                ;:install-deps :true
                ;:npm-deps []
                }

             ;; Dev Build
             ;; ----------------
             "dev" {:main 'daisneon.dais
                :output-dir "target"
                :output-to "target/main.js"
                :optimizations :none
                :source-map true
                :target :nodejs
                ;:cache-analysis true ;; defaults to `true` iff optimizations is :none
                ;:pretty-print true
                ;:static-fns true
                ;:fn-invoke-direct true
                ;:foreign-libs [{:file "src"
                ;                :provides ["dais"]}]
                ;:install-deps :true
                ;:npm-deps []
                }})

;; Do the build
(def build-mode (or (first *command-line-args*) "dev"))

(println "Performing build for:" build-mode)
(b/build "src" (get builds build-mode))

