(ns docist.sample-data.function-arities)

(defn single
  [ARG1 ARG2 ARG3]
  (println ARG1 ARG2 ARG3))

(defn multi
  ([] [:NO [:SHOW]])
  ([ARG1] [:NO [:SHOW ARG1]])
  ([ARG1 ARG2] [:NO [:SHOW ARG1 ARG2]])
  ([ARG1 ARG2 ARG3] [:NO [:SHOW ARG1 ARG2 ARG3]]))
