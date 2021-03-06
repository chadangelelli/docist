(ns docist.cljs-reader
  (:require
   [docist.utils :as utils]
   [clojure.java.io :as io]
   [cljs.analyzer :as an]
   [cljs.analyzer.api :as ana]
   [clojure.string :as string]))

(defn cljs-file?
  "Returns boolean for file ending in .cljs."
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [file]
  (and (.isFile file)
       (string/ends-with? (.getName file) ".cljs")))

(defn cljc-file?
  "Returns boolean for file ending in .cljc."
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [file]
  (and (.isFile file)
       (string/ends-with? (.getName file) ".cljc")))

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
  [fns]
  (->> (map (fn [[k {meta' :meta}]]
              [(str k)
               (assoc meta'
                      :name (str k)
                      :arglists (make-arglists (second (:arglists meta')))
                      :source "Not available for CLJS/CLJC."
                      ;; :source (slurp an/*cljs-file*)
                      :doc (utils/fix-indent (:doc meta'))
                      :related "TODO: get CLJS related")])
            fns)
       (into {})))

(defn get-fns
  "Returns functions for namespace."
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [m [dir _ files]]
  (-> (fn [m file]
        (binding [an/*analyze-deps* false]
          (let [state (ana/empty-state)
                ns-name (:ns (ana/parse-ns file))]
            (ana/no-warn (ana/analyze-file state file {}))
            (let [fns (:defs (ana/find-ns state ns-name))]
              (assoc m (str ns-name) (get-meta fns))))))
      (reduce m files)))

(defn find-namespaces
  "Returns namespaces for directory."
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [file ext]
  (let [filter-fn (if (= ext "cljs") cljs-file? cljc-file?)]
    [(str file)
     file
     (when (.isDirectory file)
       (->> (file-seq file)
            (filter filter-fn)))]))

(defn get-cljs-docs
  "Returns metadata for namespaces, functions and source.
  This is the main entry point for this namespace, as it calls all other functions."
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [paths ext]
  (->> paths
       (map #(find-namespaces (io/file %) ext))
       (reduce get-fns {})))
