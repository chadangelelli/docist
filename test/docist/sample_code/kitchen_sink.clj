(ns docist.sample-code.kitchen-sink)

;;;; ___________________________________________ PUBLIC FUNCTIONS

(defn fn-public-meta-and-docstring
  "doc:fn-public-meta-and-docstring"
  {:added "0.1" :author "alpha"}
  [a b]
  (println a b))

(defn fn-public-meta-no-docstring
  {:added "0.2" :author "bravo"}
  [a b]
  (println a b))

(defn fn-public-docstring-no-meta
  "doc:fn-public-docstring-no-meta"
  [a b]
  (println a b))

(defn fn-public-no-meta-no-docstring
  [a b]
  (println a b))

;;;; ___________________________________________ PRIVATE FUNCTIONS

#_:clj-kondo/ignore
(defn- fn-private
  "-- will never be parsed"
  [a b]
  (println a b))

;;;; ___________________________________________ PUBLIC VARS

(def ^{:added "0.3" :author "charlie"}
  var-public-meta-and-docstring
  "doc:var-public-meta-and-docstring"
  100)

(def ^{:added "0.4" :author "delta"}
  var-public-meta-no-docstring
  200)

(def var-public-docstring-no-meta
  "doc:var-public-docstring-no-meta"
  300)

(def var-public-no-meta-no-docstring
  400)

(def ^{:added "0.5" :author "echo" :doc "doc:var-public-doc-in-meta-object"}
  var-public-doc-in-meta-object
  500)

;;;; ___________________________________________ PRIVATE VARS
#_:clj-kondo/ignore
(def ^:private
  var-private
  "-- will never be parsed"
  600)
