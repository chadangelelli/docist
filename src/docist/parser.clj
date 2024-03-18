(ns docist.parser
  "Docist parser. Depending on your needs, it's possible all you need is
  `(docist.parser/parse files)` as all other functionality in this library is
  lower level."
  {:added "0.1" :author "Chad Angelelli"}
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [docist.util :as u]
            [rewrite-clj.node :as n]
            [rewrite-clj.zip :as z]))

(def ^{:added "0.1" :author "Chad Angelelli"}
  node-types
  "Parseable node type."
  #{:def :defmacro :defmulti :defmethod :defn :defonce :ns})

(def ^{:added "0.1" :author "Chad Angelelli"}
  default-parse-options
  "Default parser options."
  {:langs #{:clj :cljs}
   :index-multimethods? true}) ;TODO: add defmulti/defmethods grouping

(defn get-files
  "Returns sequence of absolute paths to parse."
  {:added "0.1" :author "Chad Angelelli"}
  ([path] (get-files path default-parse-options))
  ([path {:keys [langs]}]
   (let [f (io/file path)
         ext (re-pattern (str (string/join "|" (map name langs)) "$"))]
     (->> (if (.isDirectory f)
            (file-seq f)
            [f])
          (filter #(re-find ext (.getPath %)))))))

(defn get-docstring
  "Returns map of `{:doc string?}` for node, or `nil`."
  {:added "0.1" :author "Chad Angelelli"}
  [zloc typ]
  (when (not (some #{:defmethod :defonce} [typ]))
    (let [doc (or (-> zloc z/down (z/find-next-token #(string? (z/sexpr %))))
                  (-> zloc z/down (z/find-next-tag :multi-line)))]
      (when doc
        (z/sexpr doc)))))

(defn- -process-meta
  "Returns metadata as a consistent map. Called from `-get-meta`.

  See also: `get-meta`."
  {:added "0.1" :author "Chad Angelelli"}
  [zloc]
  (case (z/tag zloc)
    :map (z/sexpr zloc)
    :meta (let [zloc-meta (-> zloc z/down)]
            (case (z/tag zloc-meta)
              :map (z/sexpr zloc-meta)
              :token {(z/sexpr zloc-meta) true}))))

(defn get-meta
  "Return metadata for form."
  {:added "0.1" :author "Chad Angelelli"}
  [zloc typ]
  (->> zloc
       z/down
       (iterate z/right)
       (take-while (complement z/end?))
       (filter (fn [zloc]
                 (let [tag (z/tag zloc)]
                   (and (some #{:meta :map} [tag])
                        (or (= typ :ns)
                            (not (z/rightmost? zloc)))))))
       (map -process-meta)
       first))

(defn- -get-arglists
  [zloc]
  (let [first-seq-zloc (-> zloc
                           z/down
                           (z/find-next #(some #{:list :vector} [(z/tag %)])))]
    (case (z/tag first-seq-zloc)
      :vector (conj () (z/sexpr first-seq-zloc))
      :list (loop [zloc (-> zloc
                            z/down
                            z/right
                            (z/find-next #(= (z/tag %) :list))
                            z/left)
                   arglists []]
              (if (z/end? zloc)
                (seq arglists)
                (let [arglist (-> zloc
                                  (z/find-next #(= (z/tag %) :list))
                                  z/sexpr
                                  first)]
                  (recur (z/right zloc)
                         (if arglist
                           (conj arglists arglist)
                           arglists)))))
      nil)))

(defn- -parse-def
  [zloc _]
  {:type :def
   :name (-> zloc z/down z/right z/sexpr)})

(defn- -parse-defmacro
  "

  ;; WORKING NAVIGATOR for 1-ARITY TO arglists (:vector tag)
  (-> zloc z/down (z/find-next #(= (z/tag %) :vector)) z/node)

  "
  [zloc _]
  {:type :defmacro
   :name (-> zloc z/down z/right z/sexpr)
   :arglists (-get-arglists zloc)})

(defn- -parse-defmulti
  [zloc _]
  {:type :defmulti
   :name (-> zloc z/down z/right z/sexpr)})

(defn- -parse-defmethod
  [zloc _]
  {:type :defmethod
   :name (-> zloc z/down z/right z/sexpr)})

(defn- -parse-defn
  [zloc _]
  {:type :defn
   :name (-> zloc z/down z/right z/sexpr)
   :arglists (-get-arglists zloc)})

(defn- -parse-defonce
  [zloc _]
  {:type :defonce
   :name (-> zloc z/down z/right z/sexpr)})

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
  {:added "0.1" :author "Chad Angelelli"}
  ([zloc] (parse-node zloc default-parse-options))
  ([zloc options]
   (when (z/list? zloc)
     (let [typ (-> zloc z/down z/sexpr str keyword)
           meta-map (get-meta zloc typ)
           parse-node* (when-not (:private meta-map)
                         (case typ
                           :def       -parse-def
                           :defmacro  -parse-defmacro
                           :defmulti  -parse-defmulti
                           :defmethod -parse-defmethod
                           :defn      -parse-defn
                           :defonce   -parse-defonce
                           :ns        -parse-ns
                           nil))]
       (when parse-node*
         (let [parsed (parse-node* zloc options)
               ?meta-doc (:doc meta-map)
               meta-map {:meta (if ?meta-doc
                                 (dissoc meta-map :doc)
                                 meta-map)}
               docstring (get-docstring zloc typ)
               doc-map {:doc (or docstring ?meta-doc nil)}
               loc (meta (z/node zloc))]
           (merge parsed
                  meta-map
                  doc-map
                  loc)))))))

(defn get-namespace-symbol
  "Returns namespace as symbol from result of `parse-namespace`. Automatically
  called from `parse-namespace` for constructing AST."
  {:added "0.1" :author "Chad Angelelli"}
  [parsed]
  (:name (first (filter #(= (:type %) :ns) parsed))))

(defn parse-namespace
  "Returns map of `{NAMESPACE-SYMBOL PARSED}` where `PARSED` is a sequence of
  nodes as returned by `parse-node`."
  {:added "0.1" :author "Chad Angelelli"}
  ([file] (parse-namespace file default-parse-options))
  ([file _]
   (try
     (let [zloc (z/of-file file {:track-position? true})
           filepath (.getPath file)
           parsed (filter identity
                          (u/map* #(assoc (parse-node %) :file filepath)
                                  zloc))
           ns-sym (get-namespace-symbol parsed)]
       [{ns-sym parsed}])
     (catch Exception e
       [nil e]))))

(defn parse-namespaces
  "Parses all namespaces for `files` by calling `parse-namespace` on each.
  Returns vector of `[All-PARSED-NAMESPACES ?ERRORS]`."
  {:added "0.1" :author "Chad Angelelli"}
  ([files] (parse-namespaces files default-parse-options))
  ([files options]
   (loop [[file & rest-files] files, out {}, errs []]
     (if file
       (let [[parsed ?err] (parse-namespace file options)]
         (recur rest-files
                (merge out parsed)
                (if ?err (conj errs ?err) errs)))
       [out (seq errs)]))))

;;TODO: add Malli validation
(defn parse
  "Takes a seqable of paths (as strings, files or directories) and
  returns a map of `{:ast {...}, :errs [...], :options OPTIONS}`."
  {:added "0.1" :author "Chad Angelelli"}
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
