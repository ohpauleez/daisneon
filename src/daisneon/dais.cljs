(ns daisneon.dais)

;; ----------
;; This is an implementation of the Dais Interceptor Chain
;; written in ClojureScript and using ClojureScript data types (Records, keywords, persistent collections)
;;
;; There is support for static chain execution using JavaScript Arrays
;; --------------------------------------------------------------------

(enable-console-print!)

(defrecord Interceptor [name enter leave error])

(extend-protocol IPrintWithWriter
  Interceptor
  (-pr-writer [i writer _]
    (write-all writer (str "#Interceptor{:name " (.-name i) "}"))))

(defprotocol IntoInterceptor
  (-interceptor [t] "Given a value, produce an Interceptor Record."))

(extend-protocol IntoInterceptor
  PersistentHashMap
  (-interceptor [t] (map->Interceptor t))
  PersistentArrayMap
  (-interceptor [t] (map->Interceptor t))

  Interceptor
  (-interceptor [t] t)

  object
  (-interceptor [t]
    (->Interceptor (.-name t) (.-enter t) (.-leave t) (.-error t))))

(defn interceptor-name
  [n]
  (if-not (or (nil? n) (keyword? n))
    (throw (ex-info (str "Name must be keyword or nil; Got: " (pr-str n)) {:name n}))
    n))

(defn interceptor?
  [o]
  (= (type o) Interceptor))

(defn valid-interceptor?
  [o]
  (if-let [int-vals (and (interceptor? o)
                           (vals (select-keys o [:enter :leave :error])))]
    (and (some identity int-vals)
         (every? fn? (remove nil? int-vals))
         (or (interceptor-name (:name o)) true) ;; Could return `nil`
         true)
    false))

(defn interceptor
  "Given a value, produces and returns an Interceptor (Record)."
  [t]
  {:pre [(if-not (satisfies? IntoInterceptor t)
           (throw (ex-info "You're trying to use something as an interceptor that isn't supported by the protocol; Perhaps you need to extend it?"
                           {:t t
                            :type (type t)}))
           true)]
   :post [(valid-interceptor? %)]}
  (-interceptor t))

(declare handle-leave)
(declare handle-error)

(defn handle-enter [context]
  (loop [ctx context]
    (if (empty? (:dais/queue ctx))
     ctx
      (let [{queue :dais/queue
             stack :dais/stack} ctx
            interceptor (peek queue)
            old-context ctx
            new-queue (pop queue)
            ;; conj on nil returns a list, acts like a stack:
            new-stack (conj stack interceptor)
            partial-ctx (assoc ctx
                               :dais/queue new-queue
                               :dais/stack new-stack)
            enter-fn (:enter interceptor)
            new-context (if (fn? enter-fn)
                          (try
                            (enter-fn partial-ctx)
                            (catch :default e
                              (assoc (dissoc partial-ctx :dais/queue)
                                     :error e)))
                          partial-ctx)]

        (if (:error new-context)
          (handle-error new-context)
          (recur (if (some #(% new-context) (:dais/terminators new-context))
                   (handle-leave (dissoc new-context :dais/queue))
                   new-context)))))))

(defn handle-array-enter [context]
  (reduce
    (fn [ctx interceptor]
      (let [partial-ctx (update-in ctx [:dais/stack] conj interceptor)
            enter-fn (:enter interceptor)
            new-context (if (fn? enter-fn)
                          (try
                            (enter-fn partial-ctx)
                            (catch :default e
                              (assoc (dissoc partial-ctx :dais/queue)
                                     :error e)))
                          partial-ctx)]
        (if (:error new-context)
          (reduced (handle-error new-context))
          (if (some #(% new-context) (:dais/terminators new-context))
            (reduced (handle-leave (dissoc new-context :dais/queue)))
            new-context))))
    context
    (:dais/queue context)))

(defn handle-leave [context]
  (loop [ctx context]
    (if (empty? (:dais/stack ctx))
     (dissoc ctx
             :dais/stack
             "dais.stack")
      (let [stack (:dais/stack ctx)
            interceptor (peek stack)
            old-context ctx
            new-stack (pop stack)
            partial-ctx (assoc ctx
                               :dais/stack new-stack)
            leave-fn (:leave interceptor)
            new-context (if (fn? leave-fn)
                          (try
                            (leave-fn partial-ctx)
                            (catch :default e
                              (assoc (dissoc partial-ctx :dais/queue)
                                     :error e)))
                          partial-ctx)]

        (if (:error new-context)
          (handle-error new-context)
          (recur new-context))))))

(defn handle-error [context]
  (loop [ctx context]
    (if (empty? (:dais/stack ctx))
     ctx
      (let [stack (:dais/stack ctx)
            interceptor (peek stack)
            old-context ctx
            new-stack (pop stack)
            partial-ctx (assoc ctx
                               :dais/stack new-stack)
            error-fn (:error interceptor)
            new-context (if (fn? error-fn)
                          (error-fn partial-ctx)
                          partial-ctx)]

        (if (:error new-context)
          (recur new-context)
          (handle-leave new-context))))))

(defn ^:export execute
  ([context]
   {:pre [(map? context)]}
   (let [{q :dais/queue
          stack :dais/stack
          terminators :dais/terminators
          :or {stack '()
               terminators []}} context
         ;; Check for interop'd stack and terminators
         terminators (get context "dais.terminators" terminators)
         stack (get context "dais.stack" stack)]
     (cond
       (array? q) (handle-array-enter context)
       (seq q) (handle-enter (assoc context
                                    :dais/queue (if (instance? cljs.core/PersistentQueue q)
                                                  q
                                                  (into cljs.core/PersistentQueue.EMPTY q))
                                    ;; Defend against stack types and interop
                                    :dais/stack (into '() (reverse stack))
                                    :dais/terminators terminators))
       :else context)))
  ([context interceptors]
   (let [final-ctx (execute (assoc (js->clj context)
                                   :dais/queue (into cljs.core/PersistentQueue.EMPTY
                                                     (map interceptor interceptors))))]
     (if (object? context)
       (clj->js final-ctx)
       final-ctx))))

(defn ^:export hello [& args]
  "hello node - from ClojureScript")

;(println (main))

