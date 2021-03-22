(defproject docist "0.1.0-alpha"
  :description "Docist Document Generator"
  :url "http://github.com/chadangelelli/docist.git"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.764"]
                 [com.bhauman/figwheel-main "0.2.12"]
                 [com.bhauman/rebel-readline "0.1.4"]
                 [com.bhauman/rebel-readline-cljs "0.1.4"]
                 [cheshire/cheshire "5.10.0"]
                 [markdown-clj/markdown-clj "1.10.5"]]
  ;; :repl-options {:init-ns docist.core}
  )
