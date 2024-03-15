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
  [a b]
  (println a b))

(defn fn-public-docstring-no-meta
  "doc:fn-public-docstring-no-meta"
  [a b]
  (println a b))

(defn fn-public-no-meta-no-docstring
  [a b]
  (println a b))

(defn ^{:added "0.3"
        :author "fn-charlie"
        :doc "doc:fn-public-doc-in-meta-object"}
  fn-public-doc-in-meta-object
  [a b]
  (println a b))

;;;; ___________________________________________ PRIVATE FUNCTIONS

#_:clj-kondo/ignore
(defn- fn-private
  "-- will never be included in AST"
  [a b]
  (println a b))

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
  [a b]
  `(println ~a ~b))

(defmacro macro-public-meta-no-docstring
  {:added "3.1" :author "macro-bravo"}
  [a b]
  `(println ~a ~b))

(defmacro macro-public-docstring-no-meta
  "doc:macro-public-docstring-no-meta"
  [a b]
  `(println ~a ~b))

(defmacro macro-public-no-meta-no-docstring
  [a b]
  `(println ~a ~b))

(defmacro ^{:added "3.2"
        :author "macro-charlie"
        :doc "doc:macro-public-doc-in-meta-object"}
  macro-public-doc-in-meta-object
  [a b]
  `(println ~a ~b))

;;;; ___________________________________________ PUBLIC DEFONCE's

(defonce ^{:added "1.3"
           :author "var-delta"
           :doc "doc:once-var-public-doc-in-meta-object"}
  once-var-public-doc-in-meta-object
  600)

(defonce once-var-public-no-meta 600)

;;;; ___________________________________________ SPECIAL CASES

(def var-special-no-meta-with-map-as-value
  {:actual :values})

