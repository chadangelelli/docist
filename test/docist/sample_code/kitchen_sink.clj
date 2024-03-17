(ns docist.sample-code.kitchen-sink
  "Kitchen Sink examples for unit tests."
  {:added "0.1" :author "Chad Angelelli"})

;;;; ___________________________________________ PUBLIC FUNCTIONS

(defn fn-public-meta-and-docstring
  "doc:fn-public-meta-and-docstring"
  {:added "0.1" :author "fn-alpha"}
  [a b]
  (println a b))

(defn fn-public-meta-no-docstring
  {:added "0.2" :author "fn-bravo"}
  [c d]
  (println c d))

(defn fn-public-docstring-no-meta
  "doc:fn-public-docstring-no-meta"
  [e f]
  (println e f))

(defn fn-public-no-meta-no-docstring
  [g h]
  (println g h))

(defn ^{:added "0.3"
        :author "fn-charlie"
        :doc "doc:fn-public-doc-in-meta-object"}
  fn-public-doc-in-meta-object
  [i j]
  (println i j))

;;;; ___________________________________________ PRIVATE FUNCTIONS

#_:clj-kondo/ignore
(defn- fn-private
  "-- will never be included in AST"
  [k l]
  (println k l))

;;;; ___________________________________________ PUBLIC VARS

(def ^{:added "1.0" :author "var-alpha"}
  var-public-meta-and-docstring
  "doc:var-public-meta-and-docstring"
  100)

(def ^{:added "1.1" :author "var-bravo"}
  var-public-meta-no-docstring
  200)

(def var-public-docstring-no-meta
  "doc:var-public-docstring-no-meta"
  300)

(def var-public-no-meta-no-docstring
  400)

(def ^{:added "1.2"
       :author "var-charlie"
       :doc "doc:var-public-doc-in-meta-object"}
  var-public-doc-in-meta-object
  500)

;;;; ___________________________________________ PRIVATE VARS

#_:clj-kondo/ignore
(def ^:private
  var-private
  "-- will never be included in AST"
  600)

;;;; ___________________________________________ PUBLIC MULTIMETHODS

(defmulti multi-public-example1
  "doc:multi-public-example1 "
  {:added "2.0" :author "mutli-alpha"}
  identity)

(defmethod ^{:added "2.1"
             :author "multi-bravo"
             :doc "doc:multi-public-doc-in-meta"}
  multi-public-example1
  :multi-public-doc-in-meta
  [_]
  "return:multi-public-doc-in-meta")

(defmethod multi-public-example1 :multi-public-no-meta [_]
  "return:multi-public-no-meta")

;;;; ___________________________________________ PUBLIC MACOS

(defmacro macro-public-meta-and-docstring
  "doc:macro-public-meta-and-docstring"
  {:added "3.0" :author "macro-alpha"}
  [m n]
  `(println ~m ~n))

(defmacro macro-public-meta-no-docstring
  {:added "3.1" :author "macro-bravo"}
  [o p]
  `(println ~o ~p))

(defmacro macro-public-docstring-no-meta
  "doc:macro-public-docstring-no-meta"
  [q r]
  `(println ~q ~r))

(defmacro macro-public-no-meta-no-docstring
  [s t]
  `(println ~s ~t))

(defmacro ^{:added "3.2"
        :author "macro-charlie"
        :doc "doc:macro-public-doc-in-meta-object"}
  macro-public-doc-in-meta-object
  [u v]
  `(println ~u ~v))

;;;; ___________________________________________ PUBLIC DEFONCE's

(defonce ^{:added "4.0"
           :author "var-delta"
           :doc "doc:once-var-public-doc-in-meta-object"}
  once-var-public-doc-in-meta-object
  600)

(defonce once-var-public-no-meta 700)

;;;; ___________________________________________ SPECIAL CASES

(def var-special-no-meta-with-map-as-value
  {:actual :values})

(defn fn-special-empty-arglists-meta-and-docstring
  "doc:fn-special-empty-arglists-meta-and-docstring"
  {:added "5.0" :author "fn-special-alpha"}
  []
  [:this [:should [:not [:show :up]]]])

(defn fn-special-empty-arglists-meta-no-docstring
  {:added "5.1" :author "fn-special-bravo"}
  []
  [:this [:should [:not [:show :up]]]])

(defn fn-special-empty-arglists-no-meta
  []
  [:this [:should [:not [:show :up]]]])

(defn fn-special-multi-arity-arglists-meta-and-docstring
  "doc:fn-special-multi-arity-arglists-meta-and-docstring"
  {:added "5.0" :author "fn-special-alpha"}
  ([]
   [:this [:should [:not [:show :up]]]])
  ([aa bb]
   [:this [:should [:not [:show :up [aa bb]]]]]))

(defn fn-special-multi-arity-arglists-meta-no-docstring
  {:added "5.1" :author "fn-special-bravo"}
  ([]
   [:this [:should [:not [:show :up]]]])
  ([cc dd]
   [:this [:should [:not [:show :up [cc dd]]]]]))

(defn fn-special-multi-arity-arglists-no-meta
  ([]
   [:this [:should [:not [:show :up]]]])
  ([ee ff]
   [:this [:should [:not [:show :up [ee ff]]]]]))
