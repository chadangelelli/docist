(ns docist.test-util
  "Test utiltiy."
  (:require [docist.parser :as d]))

(def parser-path "src/docist/parser.clj")
(def parser-ast (atom nil))
(def parser-errs (atom nil))

(def kitchen-sink-path "test/docist/sample_code/kitchen_sink.clj")
(def kitchen-sink-ast (atom nil))
(def kitchen-sink-errs (atom nil))

(defn parse-parser
  [f]
  (let [{:keys [ast errs]} (d/parse [parser-path])]
    (reset! parser-ast ast)
    (reset! parser-errs errs)
    (f)))

(defn parse-kitchen-sink
  [f]
  (let [{:keys [ast errs]} (d/parse [kitchen-sink-path])]
    (reset! kitchen-sink-ast ast)
    (reset! kitchen-sink-errs errs)
    (f)))
