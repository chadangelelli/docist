(ns user
  (:require
    [clojure.java.io :as io]
    [clojure.tools.namespace.repl :as ns-repl]
    [clojure.repl :refer [dir doc]]
    [docist.fmt :as fmt :refer [PURPLE ORANGE NC LOGO]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; file watcher

(def mod-dirs (.list (io/file "st-modules")))

(apply ns-repl/set-refresh-dirs mod-dirs)

(defn r [] (ns-repl/refresh))
(def reload r)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; format

(def logo fmt/logo)

(defn print-init-msg []
  (println (format "REPL started at %slocalhost%s:%s16161%s"
                   PURPLE NC ORANGE NC)))

(defn fresh []
  (logo)
  (print-init-msg))
