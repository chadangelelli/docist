(ns docist.validation
  "Docist validation."
  (:require
    [malli.core :as m]
    [malli.error :as me]))

(defn invalidate
  [schema value]
  (when-let [e (m/explain schema value)]
    (me/humanize e)))
