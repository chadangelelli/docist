(ns docist.core
  (:import java.util.jar.JarFile)
  (:require
   [clojure.java.io :as io]
   [clojure.tools.namespace.find :as ns]
   [cljs.analyzer :as an]
   [cljs.analyzer.api :as ana]
   [clojure.string :as string]
   [clojure.repl :as repl]
   [cheshire.core :as json]
   [markdown.core :as md]
   [docist.clj-reader :as clj]
   [docist.cljs-reader :as cljs])
  (:import [org.apache.commons.io FileUtils]))

(defn copy-static-files!
  [base-name out-dir]
  (let [file-path (-> (.getFile (io/resource base-name))
                      (string/split #"!")
                      first
                      (string/replace "file:" ""))
        jar       (java.util.jar.JarFile. file-path)
        entries   (.entries jar)
        all-names (loop [result []]
                    (if (.hasMoreElements entries)
                      (recur (conj result (.. entries nextElement getName)))
                      result))
        names     (->> all-names
                       (filter #(string/starts-with? % base-name)))]
    (doseq [nm names]
      (let [local-name (str out-dir (string/replace nm base-name ""))
            local-file (io/file local-name)]
        (if (string/ends-with? nm "/")
          (.mkdir local-file)
          (spit local-file (slurp (io/resource nm))))))))

(defn make-js-context
  [json-clj json-cljs json-cljc readme]
  (let [tpl (string/join
             ""
             ["window.docist="
              "{"
              "\"context\":{\"clj\":%s,\"cljs\":%s,\"cljc\":%s},"
              "\"readme\":\"%s\","
              "\"search\":{\"list\":null,\"index\":null,\"engine\":null}"
              "};"])]
    (format tpl json-clj json-cljs json-cljc readme)))

(defn write-docs!
  ""
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [{:as opts dir :output-to :keys [filename]}
   clj-out
   cljs-out
   cljc-out]
  (let [json-clj  (json/generate-string clj-out)
        json-cljs (json/generate-string cljs-out)
        json-cljc (json/generate-string cljc-out)
        ctx-file  (str dir "/js/context.js")
        readme    (try (-> (slurp "README.md")
                           (md/md-to-html-string)
                           (string/replace #"\"" "\\\""))
                       (catch Exception e ""))]
    (copy-static-files! "docist/public/docist" dir)
    (spit ctx-file (make-js-context json-clj json-cljs json-cljc readme))))

(defn make-docs
  "Parses namespaces and generates JSON object"
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [{:as opts :keys [paths]}]
  (let [clj-out  (clj/get-clj-docs paths)
        cljs-out (cljs/get-cljs-docs paths 'cljs')
        cljc-out (cljs/get-cljs-docs paths 'cljc')]
    (write-docs! opts clj-out cljs-out cljc-out)
    "OK"))

(defn test! []
  (do
   (time
    (make-docs
     {:output-to "docs"
      :filename "index.html"
      :single-page? false
      :paths ["/Users/mars/Development/tec/src/tec/"]
      ;; :paths ["/Users/mars/Development/st-suite/stx/src/stx"]
      ;; :paths ["/Users/mars/Development/st-suite/st/src/st"]
      :parse ["clj" "cljs" "cljc"]}))
   nil))
