(ns docist.clj-reader
  (:import java.util.jar.JarFile)
  (:require
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
  ""
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [dir docstring]
  (let [see-also (when docstring (re-find #"[sS]ee also.*" docstring))
        fns (when see-also (vec (re-seq #"`.*?`" see-also)))]
    (when fns
      (->> fns
           (mapv #(string/replace % "`" ""))))))

(defn get-source
  ""
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [sym]
  (repl/source-fn (symbol sym)))

(defn get-indent
  "Returns integer for number of leading [\\space \\tab] characters."
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [s]
  (loop [i 0, ^StringSeq s (seq s)]
    (let [c (first s)]
      (if (or (= c \space) (= c \tab))
        (recur (inc i) (rest s))
        i))))

(defn fix-indent
  "Fixes indent by left-trimming smallest found indent from each
  line in multiline string.

  Example:

  ```
  a
    b
      c
    d
  e
  ```

  will become

  ```
  a
  b
    c
  c
  e
  ```"
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [s]
  (if (= (count s) 0)
    s
    (let [in  (->> (string/split s #"\n")
                   (map #(let [indent (get-indent %)]
                           [indent (subs % indent)])))
          is  (->> (map first in)
                  (into #{})
                  (remove zero?))
          i   (if-not (seq is) 0 (apply min is))
          lns (map #(let [[l txt] %
                            n (if (>= l i) (- l i) l)]
                        (str (apply str (repeat n " "))
                             txt))
                     in)]
      (string/join "\n" lns))))

(defn make-arglists
  [arglists]
  (mapv str arglists))

(defn get-meta
  ""
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
                          (assoc :doc (fix-indent doc))))))
      (reduce {} interns)))

(defn get-fns
  ""
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [m [dir file names]]
  (->> names
       (map #(do
               (require %)
               [(str %) (get-meta dir file (ns-interns %))]))
       (into {})))

(defn get-clj-docs
  [paths]
  (->> (map #(-> % io/file find-namespaces) paths)
       (reduce get-fns {})))
