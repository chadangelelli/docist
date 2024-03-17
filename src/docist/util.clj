(ns docist.util
  "Docist utility."
  {:added "0.1" :author "Chad Angelelli"}
  (:require [rewrite-clj.zip :as z]))

(defn map*
  "Similar to `clojure.core/map`, except iterate over a zipper `zloc`. Uses
  `rewrite-clj.zip/right` for iteration."
  {:added "0.1" :author "Chad Angelelli"}
  [f zloc]
  (->> zloc
       (iterate z/right)
       (take-while (complement z/end?))
       (map f)))

(defn filter-nodes-by-type
  "Returns sequence after filtering `nodes` by `typ`."
  [typ nodes]
  (filter #(= (:type %) typ) nodes))
