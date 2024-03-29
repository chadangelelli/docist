(ns docist.parser-test
  (:require [clojure.test :as t :refer [deftest is testing]]
            [docist.parser :as d]
            [docist.test-util :as test-util]
            [docist.util :as u]))

(t/use-fixtures :once
                test-util/parse-parser
                test-util/parse-kitchen-sink)

(deftest parser-node-types
  (testing "docist.parser/node-types"
    (is (= d/node-types
           #{:def :defmacro :defmulti :defmethod :defn :defonce :ns}))
    )) ; parser-node-types

(deftest parser-parse-errs
  (testing "No errors from parsing docist.parser"
    (is (nil? @test-util/parser-errs))
    )) ; parser-parse-errs

(deftest parser-ns
  (testing "Basic parse of Docist parser `ns` tag."
    (let [ns-node (d/get-namespace-node (get @test-util/parser-ast
                                             'docist.parser))]
      (is (= {:col 1
              :doc "Docist parser. Depending on your needs, it's possible all you need is\n  `(docist.parser/parse files)` as all other functionality in this library is\n  lower level."
              :end-col 38
              :end-row 10
              :file "src/docist/parser.clj"
              :meta {:added "0.1" :author "Chad Angelelli"}
              :name 'docist.parser
              :row 1
              :type :ns}
             ns-node))
      ))) ; end parser-ns

(deftest parser-counts
  (testing "Count number of parsed nodes in docist.parser"
    (let [nodes (get @test-util/parser-ast 'docist.parser)
          n-total (count nodes)
          n-fn's (count (u/filter-nodes-by-type :defn nodes))
          n-var's (count (u/filter-nodes-by-type :def nodes))]
      (is (= 21 n-total))
      (is (= 9 n-fn's))
      (is (= 2 n-var's))
      ))) ; end parser-counts

(deftest kitchen-sink-parse-errs
  (testing "No errors from parsing docist.sample-data.kitchen-sink"
    (is (nil? @test-util/kitchen-sink-errs))
    )) ; kitchen-sink-parse-errs

(deftest kitchen-sink-ns
  (testing "Basic parse of docist.sample-data.kitchen-sink `ns` tag."
    (let [nodes (get @test-util/kitchen-sink-ast
                     'docist.sample-code.kitchen-sink)
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
    (let [nodes      (get @test-util/kitchen-sink-ast
                          'docist.sample-code.kitchen-sink)
          n-total    (count nodes)
          n-fn's     (count (u/filter-nodes-by-type :defn nodes))
          n-var's    (count (u/filter-nodes-by-type :def nodes))
          n-multi's  (count (u/filter-nodes-by-type :defmulti nodes))
          n-method's (count (u/filter-nodes-by-type :defmethod nodes))
          n-macro's  (count (u/filter-nodes-by-type :defmacro nodes))
          n-once's   (count (u/filter-nodes-by-type :defonce nodes))]
      (is (= 32 n-total))
      (is (= 11 n-fn's))
      (is (= 6 n-var's))
      (is (= 1 n-multi's))
      (is (= 2 n-method's))
      (is (= 5 n-macro's))
      (is (= 2 n-once's))
      ))) ; end kitchen-sink-counts-test

(deftest kitchen-sink-def-forms-test
  (testing "Test def forms for equality in docist.sample-data.kitchen-sink"
    (let [nodes (get @test-util/kitchen-sink-ast
                     'docist.sample-code.kitchen-sink)
          vars (u/filter-nodes-by-type :def nodes)]
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
               :doc nil
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
               :doc nil
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
               :doc nil
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
    (let [nodes (get @test-util/kitchen-sink-ast
                     'docist.sample-code.kitchen-sink)
          macros (u/filter-nodes-by-type :defmacro nodes)]
      (is (= '{:arglists ([m n])
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
      (is (= '{:arglists ([o p])
               :col 1
               :doc nil
               :end-col 20
               :end-row 103
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta {:added "3.1" :author "macro-bravo"}
               :name macro-public-meta-no-docstring
               :row 100
               :type :defmacro}
             (second macros)))
      (is (= '{:arglists ([q r])
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
      (is (= '{:arglists ([s t])
               :col 1
               :doc nil
               :end-col 20
               :end-row 112
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta nil
               :name macro-public-no-meta-no-docstring
               :row 110
               :type :defmacro}
             (nth macros 3)))
      (is (= '{:arglists ([u v])
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
    (let [nodes (get @test-util/kitchen-sink-ast
                     'docist.sample-code.kitchen-sink)
          multis (u/filter-nodes-by-type :defmulti nodes)]
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
    (let [nodes (get @test-util/kitchen-sink-ast
                     'docist.sample-code.kitchen-sink)
          method's (u/filter-nodes-by-type :defmethod nodes)]
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
               :doc nil
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
    (let [nodes (get @test-util/kitchen-sink-ast
                     'docist.sample-code.kitchen-sink)
          fns (u/filter-nodes-by-type :defn nodes)]
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
      (is (= '{:arglists ([c d])
               :col 1
               :doc nil
               :end-col 17
               :end-row 16
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta {:added "0.2" :author "fn-bravo"}
               :name fn-public-meta-no-docstring
               :row 13
               :type :defn}
             (second fns)))
      (is (= '{:arglists ([e f])
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
      (is (= '{:arglists ([g h])
               :col 1
               :doc nil
               :end-col 17
               :end-row 25
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta nil
               :name fn-public-no-meta-no-docstring
               :row 23
               :type :defn}
               (nth fns 3)))
      (is (= '{:arglists ([i j])
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
      (is (= '{:arglists ([])
               :col 1
               :doc "doc:fn-special-empty-arglists-meta-and-docstring"
               :end-col 40
               :end-row 140
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta {:added "5.0" :author "fn-special-alpha"}
               :name fn-special-empty-arglists-meta-and-docstring
               :row 136
               :type :defn}
             (nth fns 5)))
      (is (= '{:arglists ([])
               :col 1
               :doc nil
               :end-col 40
               :end-row 145
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta {:added "5.1" :author "fn-special-bravo"}
               :name fn-special-empty-arglists-meta-no-docstring
               :row 142
               :type :defn}
             (nth fns 6)))
      (is (= '{:arglists ([])
               :col 1
               :doc nil
               :end-col 40
               :end-row 149
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta nil
               :name fn-special-empty-arglists-no-meta
               :row 147
               :type :defn}
             (nth fns 7)))
      (is (= '{:arglists ([] [aa bb])
               :col 1
               :doc "doc:fn-special-multi-arity-arglists-meta-and-docstring"
               :end-col 50
               :end-row 157
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta {:added "5.0" :author "fn-special-alpha"}
               :name fn-special-multi-arity-arglists-meta-and-docstring
               :row 151
               :type :defn}
             (nth fns 8)))
      (is (= '{:arglists ([] [cc dd])
               :col 1
               :doc nil
               :end-col 50
               :end-row 164
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta {:added "5.1" :author "fn-special-bravo"}
               :name fn-special-multi-arity-arglists-meta-no-docstring
               :row 159
               :type :defn}
             (nth fns 9)))
      (is (= '{:arglists ([] [ee ff])
               :doc nil
               :col 1
               :end-col 50
               :end-row 170
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta nil
               :name fn-special-multi-arity-arglists-no-meta
               :row 166
               :type :defn}
             (nth fns 10)))
    ))) ; end kitchen-sink-defn-forms-test

(deftest kitchen-sink-defonce-forms-test
  (testing "Test def forms for equality in docist.sample-data.kitchen-sink"
    (let [nodes (get @test-util/kitchen-sink-ast
                     'docist.sample-code.kitchen-sink)
          once's (u/filter-nodes-by-type :defonce nodes)]
      (is (= '{:col 1
               :doc "doc:once-var-public-doc-in-meta-object"
               :end-col 7
               :end-row 127
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta {:added "4.0" :author "var-delta"}
               :name once-var-public-doc-in-meta-object
               :row 123
               :type :defonce}
             (first once's)))
      (is (= '{:col 1
               :doc nil
               :end-col 38
               :end-row 129
               :file "test/docist/sample_code/kitchen_sink.clj"
               :meta nil
               :name once-var-public-no-meta
               :row 129
               :type :defonce}
             (second once's)))
      ))) ; end kitchen-sink-defonce-forms-test
