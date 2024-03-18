(ns docist.generator.markdown
  "Generate Markdown docs."
  {:added "0.1" :author "Chad Angelelli"}
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [docist.parser :as d]
            [docist.generator.util :as gu]
            [docist.util :as u]
            [selmer.parser :as template]))

(def ^{:added "0.1" :author "Chad Angelelli"}
  default-markdown-index-template
  "Default Markdown template for `index.md`."
  "resources/templates/markdown/index.md")

(def ^{:added "0.1" :author "Chad Angelelli"}
  default-markdown-namespace-template
  "Default Markdown template for `NAMESPACE.md`."
  "resources/templates/markdown/namespace.md")

(defn- -cleanup-multiple-newlines
  "Replaces 3+ newline characters with 2 newline characters."
  {:added "0.1" :author "Chad Angelelli"}
  [s]
  (string/replace s #"\n\n+" "\n\n"))

(defn- -cleanup-tables
  "Removes multiple newline characters between table rows."
  {:added "0.1" :author "Chad Angelelli"}
  [s]
  (string/replace s #"\|\n\n\|" "|\n|"))

(defn render-namespace-page
  "Produce rendered Markdown for AST nodes."
  {:added "0.1" :author "Chad Angelelli"}
  [template nodes _]
  (let [context (u/split-namespace-and-symbols nodes)]
    (-> (template/render template context)
        string/trim
        -cleanup-multiple-newlines
        -cleanup-tables)))

(defn generate-docs
  "Main Markdown generator entry point."
  {:added "0.1" :author "Chad Angelelli"}
  [ast {:keys [template] :as options}]
  (let [template (or template
                     (slurp (io/file default-markdown-namespace-template)))]
    (map (fn [[ns-sym nodes]]
           {:filename (gu/make-filename ns-sym options)
            :content (render-namespace-page template nodes options)})
         ast)))
