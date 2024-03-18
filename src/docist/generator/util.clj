(ns docist.generator.util
  "Generator utility."
  {:added "0.1" :author "Chad Angelelli"}
  (:require [clojure.string :as string]))

(defn make-filename
  "Returns filename (string) for namespace."
  {:added "0.1" :author "Chad Angelelli"}
  [clj-namespace {:keys [output-dir output-structure output-format]}]
  (let [nm (case (name output-structure)
             "flat" (str clj-namespace)
             "nested" (string/replace clj-namespace "." "/"))
        ext (case (name output-format)
              "markdown" "md"
              "html" "html")]
    (str output-dir "/" nm "." ext)))
