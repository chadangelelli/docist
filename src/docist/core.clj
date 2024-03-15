(ns docist.core
  "Core Docist entrypoint."
  {:added "0.1"
   :author "Chad Angelelli"}
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [docist.fmt :as fmt :refer [echo]]
            [rewrite-clj.node :as n]
            [rewrite-clj.zip :as z]))

(def ^{:added "0.1" :author "Chad Angelelli"}
  node-types
  "Parseable node type."
  #{:def :defmacro :defmutli :defmethod :defn :defonce :ns})

(def ^{:added "0.1" :author "Chad Angelelli"}
  default-parse-options
  "Default parser options."
  {:langs #{:clj :cljs :edn}})

(defn map*
  "Similar to `clojure.core/map`, except iterate over a zipper `zloc`. Uses
  `rewrite-clj.zip/right` for iteration."
  {:added "0.1"
   :author "Chad Angelelli"}
  [f zloc]
  (->> zloc
       (iterate z/right)
       (take-while (complement z/end?))
       (map f)))

(defn get-files
  "Returns sequence of absolute paths to parse."
  {:added "0.1"
   :author "Chad Angelelli"}
  ([path] (get-files path default-parse-options))
  ([path {:keys [langs]}]
   (let [f (io/file path)
         ext (re-pattern (str (string/join "|" (map name langs)) "$"))]
     (->> (if (.isDirectory f)
            (file-seq f)
            [f])
          (filter #(re-find ext (.getPath %)))))))

(defn get-docstring
  "Returns docstring for node. Will find `:doc` key in meta or string for
  docstring position."
  {:added "0.1"
   :author "Chad Angelelli"}
  [zloc]
  (when-let [ds (or (-> zloc z/down (z/find-next-token #(string? (z/sexpr %))))
                    (-> zloc z/down (z/find-next-tag :multi-line)))]
    (z/sexpr ds)))

(defn- -process-meta
  "Returns metadata as a consistent map. Called from `-get-meta`.

  See also: `-get-meta`."
  {:author "Chad A." :added "0.1"}
  [zloc]
  (case (z/tag zloc)
    :map (z/sexpr zloc)
    :meta (let [zloc-meta (-> zloc z/down)]
            (case (z/tag zloc-meta)
              :map (z/sexpr zloc-meta)
              :token {(z/sexpr zloc-meta) true}))))

(defn get-meta
  "Return metadata for form."
  {:added "0.1"
   :author "Chad Angelelli"}
  [zloc]
  (->> zloc
       z/down
       (iterate z/right)
       (take-while (complement z/end?))
       (filter #(some #{:meta :map} [(z/tag %)]))
       (map -process-meta)
       first))

(defn- -parse-var
  [zloc _]
  {:type :def
   :name (-> zloc z/down z/right z/sexpr)})

(defn- -parse-defmacro
  [zloc _]
  )

(defn- -parse-defmulti
  [zloc _]
  )

(defn- -parse-defmethod
  [zloc _]
  )

(defn- -parse-defn
  [zloc _]
  (let [fn-name (-> zloc z/down z/right z/sexpr)
        arglists (->> zloc
                      z/down
                      (iterate z/right)
                      (take-while (complement z/end?))
                      (filter #(= (z/tag %) :vector))
                      (map z/sexpr))]
    {:type :defn
     :name fn-name
     :arglists arglists}))

(defn- -parse-defonce
  [zloc _]
  )

(defn- -parse-ns
  [zloc _]
  (let [ns-sym (or (-> zloc z/down (z/find-next-tag :token) z/sexpr)
                   (-> zloc
                       z/down
                       (z/find-next-tag :meta)
                       z/node
                       n/children
                       last
                       str
                       symbol))]
    {:type :ns
     :name ns-sym}))

(defn parse-node
  "Parses node at zipper location `zloc`. Returns map of the form:

  ```clojure
  {:type symbol?
  :name string?
  :doc string?
  :meta map?
  :row int?
  :col int?
  :end-row int?
  :end-col int?}
  ```

  If `:type` is `:fn`, `:method` or `:macro`, additionally `:arglists`
  will be added."
  {:added "0.1"
   :author "Chad Angelelli"}
  ([zloc] (parse-node zloc default-parse-options))
  ([zloc options]
   (when (z/list? zloc)
     (let [typ (-> zloc z/down z/sexpr str)
           loc (meta (z/node zloc))
           doc-map {:doc (get-docstring zloc)}
           meta-map {:meta (get-meta zloc)}
           f (case (keyword typ)
               :def       -parse-var
               :defmacro  -parse-defmacro
               :defmutli  -parse-defmulti
               :defmethod -parse-defmethod
               :defn      -parse-defn
               :defonce   -parse-defonce
               :ns        -parse-ns
               nil)]
       (when f
         (merge (f zloc options)
                doc-map
                meta-map
                loc))))))

(defn get-namespace-symbol
  "Returns namespace as symbol from result of `parse-namespace`. Automatically
  called from `parse-namespace` for constructing AST."
  {:added "0.1"
   :author "Chad Angelelli"}
  [parsed]
  (:name (first (filter #(= (:type %) :ns) parsed))))

(defn parse-namespace
  "Returns map of `{NAMESPACE-SYMBOL PARSED}` where `PARSED` is a sequence of
  nodes as returned by `parse-node`."
  {:added "0.1"
   :author "Chad Angelelli"}
  ([file] (parse-namespace file default-parse-options))
  ([file options]
   (try
     (let [zloc (z/of-file file {:track-position? true})
           parsed (filter identity (map* parse-node zloc))
           ns-sym (get-namespace-symbol parsed)]
       [{ns-sym parsed}])
     (catch Exception e
       [nil e]))))

(defn parse-namespaces
  "Parses all namespaces for `files` by calling `parse-namespace` on each.
  Returns vector of `[All-PARSED-NAMESPACES ?ERRORS]`."
  {:added "0.1"
   :author "Chad Angelelli"}
  ([files] (parse-namespaces files default-parse-options))
  ([files options]
   (loop [[file & rest-files] files, out {}, errs []]
     (if file
       (let [[parsed ?err] (parse-namespace file options)]
         (recur rest-files
                (merge out parsed)
                (if ?err (conj errs ?err) errs)))
       [out (seq errs)]))))

(defn parse
  "Takes a seqable of paths as strings or file objects (file or directory)
  and returns a map of `{:ast {...}, :errs [...], :options OPTIONS}`."
  {:added "0.1"
   :author "Chad Angelelli"}
  ([paths] (parse paths default-parse-options))
  ([paths options]
   (loop [[path & rest-paths] paths, ast {}, errs []]
     (if path
       (let [namespaces (get-files path)
             [parsed-namespaces ?errs] (parse-namespaces namespaces options)
             ast (merge ast parsed-namespaces)]
         (recur rest-paths ast (if ?errs (into errs ?errs) errs)))
       {:ast ast
        :errs (seq errs)
        :options options}))))

(defn get-namespace-node
  "Returns namespace node for `nodes` as returned from `parse-node`."
  [nodes]
  (first (filter #(= (:type %) :ns) nodes)))

(defn filter-nodes-by-type
  "Returns sequence after filtering `nodes` by `typ`."
  [typ nodes]
  (filter #(= (:type %) typ) nodes))
