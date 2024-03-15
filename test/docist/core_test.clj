(ns docist.core-test
  (:require [clojure.test :as t :refer [deftest is testing]]
            [docist.core :as d]))

(def core-ast (atom nil))
(def core-errs (atom nil))
(def kitchen-sink-ast (atom nil))
(def kitchen-sink-errs (atom nil))

(defn parse-core
  [f]
  (let [core-path "src/docist/core.clj"
        {:keys [ast errs]} (d/parse [core-path])]
    (reset! core-ast ast)
    (reset! core-errs errs)
    (f)))

(defn parse-kitchen-sink
  [f]
  (let [kitchen-sink-path "test/docist/sample_code/kitchen_sink.clj"
        {:keys [ast errs]} (d/parse [kitchen-sink-path])]
    (reset! kitchen-sink-ast ast)
    (reset! kitchen-sink-errs errs)
    (f)))

(t/use-fixtures :once
                parse-core
                parse-kitchen-sink)

(deftest core-node-types1
  (testing "docist.core/node-types"
    (is (= d/node-types
           #{:def :defmacro :defmutli :defmethod :defn :defonce :ns}))
    )) ; core-node-types1

(deftest core-parse-errs1
  (testing "No errors from parsing docist.core"
    (is (nil? @core-errs))
    )) ; core-parse-errs1

(deftest core-ns-test1
  (testing "Basic parse of Docist core `ns` tag."
    (let [ns-node (d/get-namespace-node (get @core-ast 'docist.core))]
      (is (= {:col 1
              :doc "Core Docist entrypoint."
              :end-col 38
              :end-row 9
              :meta {:added "0.1" :author "Chad Angelelli"}
              :name 'docist.core
              :row 1
              :type :ns}
             ns-node))
      ))) ; end core-ns-test1

(deftest core-counts1
  (testing "Count number of parsed nodes in docist.core"
    (let [nodes (get @core-ast 'docist.core)
          n-total (count nodes)
          n-fn's (count (d/filter-nodes-by-type :defn nodes))
          n-var's (count (d/filter-nodes-by-type :def nodes))]
      (is (= n-total 14))
      (is (= n-fn's 11))
      (is (= n-var's 2))
      ))) ; end core-counts1

(deftest kitchen-sink-parse-errs1
  (testing "No errors from parsing docist.sample-data.kitchen-sink"
    (is (nil? @kitchen-sink-errs))
    )) ; kitchen-sink-parse-errs1

