(ns docist.clj-reader
  (:import java.util.jar.JarFile)
  (:require
   [docist.utils :as utils]
   [clojure.java.io :as io]
   [clojure.tools.namespace.find :as ns]
   [cljs.analyzer :as an]
   [cljs.analyzer.api :as ana]
   [clojure.string :as string]
   [clojure.repl :as repl]
   [cheshire.core :as json]
   [markdown.core :as md])
  (:import [org.apache.commons.io FileUtils]))

(defn jar-file?
  "Returns true if file is a JAR."
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [file]
  (and (.isFile file)
       (-> file .getName (.endsWith ".jar"))))

(defn find-namespaces
  "Returns namespaces in directories and JAR files.

  See also: `docist.core/get-namespaces`, `docist.core/get-meta`."
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [file]
  [(str file)
   file
   (cond
     (.isDirectory file) (ns/find-namespaces-in-dir file)
     (jar-file? file)    (ns/find-namespaces-in-jarfile (JarFile. file)))])

(defn get-related
  "Returns related functions set as:

  ```
  DOCSTRING
  ...
  See also: `namespace-1/fn-name`, `namespace-2/fn-name`, ..
  ```"
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [dir docstring]
  (let [see-also (when docstring (re-find #"[sS]ee also.*" docstring))
        fns (when see-also (vec (re-seq #"`.*?`" see-also)))]
    (when fns
      (->> fns
           (mapv #(string/replace % "`" ""))))))

(defn get-source
  "Returns source code for function (if possible)."
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [sym]
  (repl/source-fn (symbol sym)))

(defn make-arglists
  "Returns a vector of stringified arglists."
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [arglists]
  (mapv str arglists))

(defn get-meta
  "Returns metadata for a function.

  The format is as follows:

  ```clojure
  {:arglists [<signature-1>, <signature-n> ..]
   :source <if-applicable>
   :related <if-applicable>
   :doc <STRING>
   :line <INT>
   :col <INT>}
  ```"
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [dir file interns]
  (-> (fn [m [k v]]
        (let [{:as meta* :keys [doc]} (meta v)
              src-sym (symbol (str (:ns meta*)) (name k))
              k* (name k)]
          (assoc m k* (-> meta*
                          (dissoc :ns)
                          (dissoc :protocol)
                          (assoc :arglists (make-arglists (:arglists meta*)))
                          (assoc :source (get-source src-sym))
                          (assoc :related (get-related dir doc))
                          (assoc :doc (utils/fix-indent doc))))))
      (reduce {} interns)))

(defn get-fns
  "Returns functions for a namespace."
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [m [dir file names]]
  (->> names
       (map #(do
               (require %)
               [(str %) (get-meta dir file (ns-interns %))]))
       (into {})))

(defn get-clj-docs
  "Returns metadata for namespaces, functions and source.
  This is the main entry point for this namespace, as it calls all other functions."
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [paths]
  (->> (map #(-> % io/file find-namespaces) paths)
       (reduce get-fns {})))
