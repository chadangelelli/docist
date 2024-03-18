(ns docist.generator
  "Docist documentation generator.

  _TIP_: A zero-artity function can be provided for any `option` in the
  `options` map, with the exception of the following:

  - `:directory-structure` -- This can be one of `#{:flat :nested}`.
  - `:output-format` -- The generators are built-in.

  _TIP_: To create your own generator, call `docist.parser/parse` and process
  the vanilla Clojure `:ast` map in the return value."
  {:added "0.1" :author "Chad Angelelli"}
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [docist.generator.html :as html]
            [docist.generator.markdown :as markdown]
            [docist.validation :as v]
            [malli.core :as m]))

;;TODO: Add Themes, where all options are determined by the Theme itself.
(def ^{:added "0.1" :author "Chad Angelelli"}
  default-generator-options
  "Default generator options."
  {:output-dir "docs"
   :output-format :markdown
   :output-structure :flat})

;;TODO: add Wikilinks for See-also's
(defn generate-docs
  "Generates sequence of filename's and file content. Returns a sequence of
  `([FILENAME FILE-CONTENT] [FILENAME FILE-CONTENT] ..)`."
  {:added "0.1" :author "Chad Angelelli"}
  ([ast] (generate-docs ast default-generator-options))
  ([ast {:keys [output-format] :as options}]
   (case (name output-format)
     "markdown" (markdown/generate-docs ast options)
     "html" (html/generate-docs ast options))))

;;TODO: consider validating AST
(def ^{:added "0.1" :author "Chad Angelelli"}
  Valid-Geneate!-Options
  (m/schema
    [:map {:closed false}
     [:output-format {:optional true} [:enum :html :markdown]]
     [:output-dir {:optional true} string?]
     [:output-structure {:optional true} [:enum :flat :nested]]
     [:template {:optional true} string?]]))

(defn generate!
  "Generates docs. Writes files to disk. Returns sequence of paths written."
  {:added "0.1" :author "Chad Angelelli"}
  ([ast] (generate! ast default-generator-options))
  ([ast options]
   (if-let [err (v/invalidate Valid-Geneate!-Options options)]
     (throw (Exception. (str err)))
     (let [options* (merge default-generator-options options)
           docs (generate-docs ast options*)]
       (doseq [{:keys [filename content]} docs
               :let [dir (string/replace filename #"/.+\.clj[s]?$" "")]]
         (when-not (.exists (io/file dir))
           (io/make-parents filename))
         (spit (io/file filename) content))
       (map :filename docs)))))
