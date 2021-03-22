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
  "Copies files from installed JAR file.
  If `:bootstrapped?` is set to true, will read files from local docist dir."
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [opts base-name out-dir]
  (if (:bootstrapped? opts)
    (FileUtils/copyDirectory (io/file "resources/docist/public/docist")
                             (io/file "docs"))
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
            (spit local-file (slurp (io/resource nm)))))))))

(defn make-js-context
  "Returns `window.docist` map containing:

  ```javascript
  window.docist = {
    context: {
      clj: CLJ_CONTEXT,
      cljs: CLJS_CONTEXT,
      cljc: CLJC_CONTEXT
    },
    readme: \"RENDERED README HTML\",
    search: {
      list: FLATTENED_CONTEXT_FOR_FUSE_SEARCH,
      index: FUSE_GENERATED_INDEX,
      engine: FUSE,
      result: POPULATED_VIA_FUSE
    }
  }
  ```
  "
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [opts json-clj json-cljs json-cljc readme]
  (let [tpl (string/join
             ""
             ["window.docist="
              "{"
              "\"context\":{\"clj\":%s,\"cljs\":%s,\"cljc\":%s},"
              "\"readme\":\"%s\","
              "\"search\":{\"list\":null,\"index\":null,\"engine\":null}"
              "};"])]
    (format tpl json-clj json-cljs json-cljc readme)))

;;TODO: fix parsing README and escape chars
(defn write-docs!
  "Copies static files to `:output-to` dir and
  writes `window.docist` object for JS."
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
                           ;; (string/replace #"\"" "\\\"")
                           (string/escape {\& "&amp;"
                                           ;; \< "&lt;"
                                           ;; \> "&gt;"
                                           \" "&quot;"
                                           \' "&#39;"
                                           \newline "\\n"})
                           )
                       (catch Exception e ""))]
    (copy-static-files! opts "docist/public/docist" dir)
    (spit ctx-file (make-js-context opts json-clj json-cljs json-cljc readme))))

(defn make-docs
  "Parses namespaces and generates JSON object for use in SPA.

  ```clojure
  (make-docs
   {:output-to \"docs\"
    :filename \"index.html\"
    :single-page? false
    :paths [\"src\"]
    :parse [\"clj\" \"cljs\" \"cljc\"]})
  ```"
  ^{:author "Chad Angelelli"
    :added "0.1.0"}
  [{:as opts :keys [paths parse]}]
  (let [parse-opts (reduce #(assoc %1 (keyword (name %2)) true) {} parse)
        clj-out  (if (:clj  parse-opts) (clj/get-clj-docs paths) "")
        cljs-out (if (:cljs parse-opts) (cljs/get-cljs-docs paths "cljs") "")
        cljc-out (if (:cljc parse-opts) (cljs/get-cljs-docs paths "cljc") "")]
    (write-docs! opts clj-out cljs-out cljc-out)
    "OK"))

;;TODO: allow custom filename
;;TODO: minify all code into a single HTML file (:single-page? true)
(defn test! []
  (do
   (time
    (make-docs
     {:output-to "docs"
      :bootstrapped? true
      :filename "index.html"
      :single-page? false
      :paths ["src"]
      :parse ["clj" "cljs" "cljc"]}))
   nil))
