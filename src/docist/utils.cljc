(ns docist.utils
  (:require
   [clojure.string :as string]))


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

  ```clojure
  a
    b
      c
    d
  e
  ```

  will become

  ```clojure
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
