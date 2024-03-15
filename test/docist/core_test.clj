(ns docist.core-test
  (:require [clojure.test :as t :refer [deftest is testing]]
            [docist.core :as d]))

(def core-path "src/docist/core.clj")
(def core-ast (atom nil))
(def core-errs (atom nil))

(def kitchen-sink-path "test/docist/sample_code/kitchen_sink.clj")
(def kitchen-sink-ast (atom nil))
(def kitchen-sink-errs (atom nil))

(defn parse-core
  [f]
  (let [{:keys [ast errs]} (d/parse [core-path])]
    (reset! core-ast ast)
    (reset! core-errs errs)
    (f)))

(defn parse-kitchen-sink
  [f]
  (let [{:keys [ast errs]} (d/parse [kitchen-sink-path])]
    (reset! kitchen-sink-ast ast)
    (reset! kitchen-sink-errs errs)
    (f)))

(t/use-fixtures :once
                parse-core
                parse-kitchen-sink)

(deftest core-node-types
  (testing "docist.core/node-types"
    (is (= d/node-types
           #{:def :defmacro :defmulti :defmethod :defn :defonce :ns}))
    )) ; core-node-types

(deftest core-parse-errs
  (testing "No errors from parsing docist.core"
    (is (nil? @core-errs))
    )) ; core-parse-errs

(deftest core-ns
  (testing "Basic parse of Docist core `ns` tag."
    (let [ns-node (d/get-namespace-node (get @core-ast 'docist.core))]
      (is (= {:col 1
              :doc "Core Docist entrypoint."
              :end-col 38
              :end-row 7
              :file "src/docist/core.clj"
              :meta {:added "0.1" :author "Chad Angelelli"}
              :name 'docist.core
              :row 1
              :type :ns}
             ns-node))
      ))) ; end core-ns

(deftest core-counts
  (testing "Count number of parsed nodes in docist.core"
    (let [nodes (get @core-ast 'docist.core)
          n-total (count nodes)
          n-fn's (count (d/filter-nodes-by-type :defn nodes))
          n-var's (count (d/filter-nodes-by-type :def nodes))]
      (is (= 22 n-total))
      (is (= 11 n-fn's))
      (is (= 2 n-var's))
      ))) ; end core-counts

(deftest kitchen-sink-parse-errs
  (testing "No errors from parsing docist.sample-data.kitchen-sink"
    (is (nil? @kitchen-sink-errs))
    )) ; kitchen-sink-parse-errs

(deftest kitchen-sink-ns
  (testing "Basic parse of docist.sample-data.kitchen-sink `ns` tag."
    (let [nodes (get @kitchen-sink-ast 'docist.sample-code.kitchen-sink)
          ns-node (d/get-namespace-node nodes)]
      (is (= {:name 'docist.sample-code.kitchen-sink
              :type :ns
              :doc "Kitchen Sink examples for unit tests."
              :meta {:added "0.1" :author "Chad Angelelli"}
              :row 1
              :col 1
              :end-row 3
              :file "test/docist/sample_code/kitchen_sink.clj"
              :end-col 43}
             ns-node))
      ))) ; end kitchen-sink-ns

(deftest kitchen-sink-counts-test
  (testing "Count number of parsed nodes in docist.sample-data.kitchen-sink"
    (let [nodes      (get @kitchen-sink-ast 'docist.sample-code.kitchen-sink)
          n-total    (count nodes)
          n-fn's     (count (d/filter-nodes-by-type :defn nodes))
          n-var's    (count (d/filter-nodes-by-type :def nodes))
          n-multi's  (count (d/filter-nodes-by-type :defmulti nodes))
          n-method's (count (d/filter-nodes-by-type :defmethod nodes))
          n-macro's  (count (d/filter-nodes-by-type :defmacro nodes))
          n-once's   (count (d/filter-nodes-by-type :defonce nodes))]
      (is (= 26 n-total))
      (is (= 5 n-fn's))
      (is (= 6 n-var's))
      (is (= 1 n-multi's))
      (is (= 2 n-method's))
      (is (= 5 n-macro's))
      (is (= 2 n-once's))
      ))) ; end kitchen-sink-counts-test

(deftest kitchen-sink-def-forms-test
  (testing "Test def forms for equality in docist.sample-data.kitchen-sink"
    (let [nodes (get @kitchen-sink-ast 'docist.sample-code.kitchen-sink)
          vars (d/filter-nodes-by-type :def nodes)]
      (is (= '{:col 1
               :doc "doc:var-public-meta-and-docstring"
               :end-col 7
               :end-row 47
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta {:added "1.0" :author "var-alpha"}
               :name var-public-meta-and-docstring
               :row 44
               :type :def}
             (first vars)))
      (is (= '{:col 1
               :end-col 7
               :end-row 51
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta {:added "1.1" :author "var-bravo"}
               :name var-public-meta-no-docstring
               :row 49
               :type :def}
             (second vars)))
      (is (= '{:col 1
               :doc "doc:var-public-docstring-no-meta"
               :end-col 7
               :end-row 55
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta nil
               :name var-public-docstring-no-meta
               :row 53
               :type :def}
             (nth vars 2)))
      (is (= '{:col 1
               :end-col 7
               :end-row 58
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta nil
               :name var-public-no-meta-no-docstring
               :row 57
               :type :def}
             (nth vars 3)))
      (is (= '{:col 1
               :doc "doc:var-public-doc-in-meta-object"
               :end-col 7
               :end-row 64
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta {:added "1.2" :author "var-charlie"}
               :name var-public-doc-in-meta-object
               :row 60
               :type :def}
             (nth vars 4)))
      (is (= '{:col 1
               :end-col 21
               :end-row 134
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta nil
               :name var-special-no-meta-with-map-as-value
               :row 133
               :type :def}
             (nth vars 5)))
    ))) ; end kitchen-sink-def-forms-test

(deftest kitchen-sink-defmacro-forms-test
  (testing "Test def forms for equality in docist.sample-data.kitchen-sink"
    (let [nodes (get @kitchen-sink-ast 'docist.sample-code.kitchen-sink)
          macros (d/filter-nodes-by-type :defmacro nodes)]
      (is (= '{:arglists ([a b])
               :col 1
               :doc "doc:macro-public-meta-and-docstring"
               :end-col 20
               :end-row 98
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta {:added "3.0" :author "macro-alpha"}
               :name macro-public-meta-and-docstring
               :row 94
               :type :defmacro}
             (first macros)))
      (is (= '{:arglists ([a b])
               :col 1
               :end-col 20
               :end-row 103
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta {:added "3.1" :author "macro-bravo"}
               :name macro-public-meta-no-docstring
               :row 100
               :type :defmacro}
             (second macros)))
      (is (= '{:arglists ([a b])
               :col 1
               :doc "doc:macro-public-docstring-no-meta"
               :end-col 20
               :end-row 108
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta nil
               :name macro-public-docstring-no-meta
               :row 105
               :type :defmacro}
             (nth macros 2)))
      (is (= '{:arglists ([a b])
               :col 1
               :end-col 20
               :end-row 112
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta nil
               :name macro-public-no-meta-no-docstring
               :row 110
               :type :defmacro}
             (nth macros 3)))
      (is (= '{:arglists ([a b])
               :col 1
               :doc "doc:macro-public-doc-in-meta-object"
               :end-col 20
               :end-row 119
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta {:added "3.2" :author "macro-charlie"}
               :name macro-public-doc-in-meta-object
               :row 114
               :type :defmacro}
             (nth macros 4)))
      ))) ; end kitchen-sink-defmacro-forms-test

(deftest kitchen-sink-defmulti-forms-test
  (testing "Test def forms for equality in docist.sample-data.kitchen-sink"
    (let [nodes (get @kitchen-sink-ast 'docist.sample-code.kitchen-sink)
          multis (d/filter-nodes-by-type :defmulti nodes)]
      (is (= '{:col 1
               :doc "doc:multi-public-example1 "
               :end-col 12
               :end-row 79
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta {:added "2.0" :author "mutli-alpha"}
               :name multi-public-example1
               :row 76
               :type :defmulti}
             (first multis)))
      ))) ; end kitchen-sink-defmulti-forms-test

(deftest kitchen-sink-defmethod-forms-test
  (testing "Test def forms for equality in docist.sample-data.kitchen-sink"
    (let [nodes (get @kitchen-sink-ast 'docist.sample-code.kitchen-sink)
          method's (d/filter-nodes-by-type :defmethod nodes)]
      (is (= '{:col 1
               :doc "doc:multi-public-doc-in-meta"
               :end-col 37
               :end-row 87
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta {:added "2.1" :author "multi-bravo"}
               :name multi-public-example1
               :row 81
               :type :defmethod}
             (first method's)))
      (is (= '{:col 1
               :end-col 33
               :end-row 90
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta nil
               :name multi-public-example1
               :row 89
               :type :defmethod}
             (second method's)))
      ))) ; end kitchen-sink-defmethod-forms-test

(deftest kitchen-sink-defn-forms-test
  (testing "Test defn forms for equality in docist.sample-data.kitchen-sink"
    (let [nodes (get @kitchen-sink-ast 'docist.sample-code.kitchen-sink)
          fns (d/filter-nodes-by-type :defn nodes)]
      (is (= '{:arglists ([a b])
               :col 1
               :doc "doc:fn-public-meta-and-docstring"
               :end-col 17
               :end-row 11
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta {:added "0.1" :author "fn-alpha"}
               :name fn-public-meta-and-docstring
               :row 7
               :type :defn}
             (first fns)))
      (is (= '{:arglists ([a b])
               :col 1
               :end-col 17
               :end-row 16
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta {:added "0.2" :author "fn-bravo"}
               :name fn-public-meta-no-docstring
               :row 13
               :type :defn}
             (second fns)))
      (is (= '{:arglists ([a b])
               :col 1
               :doc "doc:fn-public-docstring-no-meta"
               :end-col 17
               :end-row 21
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta nil
               :name fn-public-docstring-no-meta
               :row 18
               :type :defn}
             (nth fns 2)))
      (is (= '{:arglists ([a b])
               :col 1
               :end-col 17
               :end-row 25
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta nil
               :name fn-public-no-meta-no-docstring
               :row 23
               :type :defn}
               (nth fns 3)))
      (is (= '{:arglists ([a b])
               :col 1
               :doc "doc:fn-public-doc-in-meta-object"
               :end-col 17
               :end-row 32
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta {:added "0.3" :author "fn-charlie"}
               :name fn-public-doc-in-meta-object
               :row 27
               :type :defn}
             (nth fns 4)))
    ))) ; end kitchen-sink-defn-forms-test

(deftest kitchen-sink-defonce-forms-test
  (testing "Test def forms for equality in docist.sample-data.kitchen-sink"
    (let [nodes (get @kitchen-sink-ast 'docist.sample-code.kitchen-sink)
          once's (d/filter-nodes-by-type :defonce nodes)]
      (is (= '{:col 1
               :doc "doc:once-var-public-doc-in-meta-object"
               :end-col 7
               :end-row 127
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta {:added "1.3" :author "var-delta"}
               :name once-var-public-doc-in-meta-object
               :row 123
               :type :defonce}
             (first once's)))
      (is (= '{:col 1
               :end-col 38
               :end-row 129
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta nil
               :name once-var-public-no-meta
               :row 129
               :type :defonce}
             (second once's)))
      ))) ; end kitchen-sink-defonce-forms-test

