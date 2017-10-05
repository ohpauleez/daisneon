(ns daisneon.example
  (:require [daisneon.dais :as dais]))

(def inter-a (dais/interceptor {:enter #(assoc % :a 1)
                                :leave #(assoc % :leave-a 11)}))
(def inter-b (dais/interceptor {:enter #(assoc % :b 2)}))
(def inter-c (dais/interceptor {:enter #(assoc % :c 3)}))

(def queue-chain (into cljs.core/PersistentQueue.EMPTY [inter-a
                                                        inter-b
                                                        inter-c]))
(def array-chain #js[inter-a inter-b inter-c])

;; For the most part,
;; these are all 0.08 - 0.3 msecs
;; --------------------------------

;; 0.1 - 0.31 msecs
(defn ^:export example []
  (let [context {:dais/terminators [#(:b %)]
                 :dais/queue [{:enter #(assoc % :a 1)
                               :leave #(assoc % :leave-a 11)}
                              {:enter #(assoc % :b 2)}
                              {:enter #(assoc % :c 3)}]}]
    (dais/execute context)))

;; 0.1 - 0.30 msecs
(defn ^:export example1 []
  (let [context {:dais/terminators [#(:b %)]
                 :dais/queue [inter-a
                              inter-b
                              inter-c]}]
    (dais/execute context)))

;; 0.09 - 0.29 msecs
(defn ^:export example1b []
  (let [context {:dais/terminators [#(:b %)]
                 :dais/queue queue-chain}]
    (dais/execute context)))

;; 0.07 - 0.30 msecs
(defn ^:export exampleStatic []
  (let [context {:dais/terminators [#(:b %)]
                 :dais/queue #js[{:enter #(assoc % :a 1)
                                  :leave #(assoc % :leave-a 11)}
                                 {:enter #(assoc % :b 2)}
                                 {:enter #(assoc % :c 3)}]}]
    (dais/execute context)))

;; 0.06 - 0.28 msecs
(defn ^:export exampleStatic1 []
  (let [context {:dais/terminators [#(:b %)]
                 :dais/queue #js[inter-a
                                 inter-b
                                 inter-c]}]
    (dais/execute context)))

;; 0.06 - 0.26 msecs
(defn ^:export exampleStatic1b []
  (let [context {:dais/terminators [#(:b %)]
                 :dais/queue array-chain}]
    (dais/execute context)))

