(ns docist.generator-test
  (:require [clojure.test :as t :refer [deftest is testing]]
            [docist.parser :as d]
            [docist.generator :as g]
            [docist.test-util :as test-util]
            [docist.util :as u]))

(t/use-fixtures :once
                test-util/parse-parser
                test-util/parse-kitchen-sink)

(deftest docist-parser-ns-node-with-default-options
  (testing "docist.parser generated with default options"
    (let [ast-ns-only {'docist.parser (->> (get @test-util/parser-ast
                                                'docist.parser)
                                           (u/filter-nodes-by-type :ns))}
          out (g/generate-docs ast-ns-only)]
      (is (= '({:content "# docist.parser\n\nDocist parser. Depending on your needs, it's possible all you need is\n  `(docist.parser/parse files)` as all other functionality in this library is\n  lower level.\n\n**Location & Metadata**\n\n| Key  | Value |\n| ---- | ----- |\n| File | src/docist/parser.clj |\n| `:added` | 0.1 |\n| `:author` | Chad Angelelli |",
                :filename "docs/docist.parser.md"})
             out))
      ))) ; end docist-parser-ns-node-with-default-options

(deftest file-write-with-default-options
  (let [{:keys [ast errs]} (d/parse [test-util/generator-util-path])
        files (g/generate! ast {:output-dir "/tmp/docist-gen-test"})
        n-files (count files)
        filename (first files)
        file-contents (slurp filename)]
    (is (nil? errs))
    (is (= "/tmp/docist-gen-test/docist.generator.util.md" filename))
    (is (= n-files 1))
    (is (= "# docist.generator.util\n\nGenerator utility.\n\n**Location & Metadata**\n\n| Key  | Value |\n| ---- | ----- |\n| File | src/docist/generator/util.clj |\n| `:added` | 0.1 |\n| `:author` | Chad Angelelli |\n\n## make-filename\n\nReturns filename (string) for namespace.\n\n**Location & Metadata**\n\n| Key | Value                       |\n| --- | --------------------------- |\n| Lines | 6 - 16 |\n| `:added` | 0.1 |\n| `:author` | Chad Angelelli |"
           file-contents))
    )) ; end file-write-with-default-options

(deftest docist-parser-all-nodes-with-default-options
  (testing "docist.parser generated with default options"
    (let [parser-ast {'docist.parser (get @test-util/parser-ast
                                          'docist.parser)}
          out (g/generate-docs parser-ast)]
      (is (= '({:content "# docist.parser\n\nDocist parser. Depending on your needs, it's possible all you need is\n  `(docist.parser/parse files)` as all other functionality in this library is\n  lower level.\n\n**Location & Metadata**\n\n| Key  | Value |\n| ---- | ----- |\n| File | src/docist/parser.clj |\n| `:added` | 0.1 |\n| `:author` | Chad Angelelli |\n\n## node-types\n\nParseable node type.\n\n**Location & Metadata**\n\n| Key | Value                       |\n| --- | --------------------------- |\n| Lines | 12 - 15 |\n| `:added` | 0.1 |\n| `:author` | Chad Angelelli |\n\n## default-parse-options\n\nDefault parser options.\n\n**Location & Metadata**\n\n| Key | Value                       |\n| --- | --------------------------- |\n| Lines | 17 - 21 |\n| `:added` | 0.1 |\n| `:author` | Chad Angelelli |\n\n## get-files\n\nReturns sequence of absolute paths to parse.\n\n**Location & Metadata**\n\n| Key | Value                       |\n| --- | --------------------------- |\n| Lines | 23 - 33 |\n| `:added` | 0.1 |\n| `:author` | Chad Angelelli |\n\n## get-docstring\n\nReturns map of `{:doc string?}` for node, or `nil`.\n\n**Location & Metadata**\n\n| Key | Value                       |\n| --- | --------------------------- |\n| Lines | 35 - 43 |\n| `:added` | 0.1 |\n| `:author` | Chad Angelelli |\n\n## get-meta\n\nReturn metadata for form.\n\n**Location & Metadata**\n\n| Key | Value                       |\n| --- | --------------------------- |\n| Lines | 58 - 72 |\n| `:added` | 0.1 |\n| `:author` | Chad Angelelli |\n\n## parse-node\n\nParses node at zipper location `zloc`. Returns map of the form:\n\n  ```clojure\n  {:type symbol?\n  :name string?\n  :doc string?\n  :meta map?\n  :row int?\n  :col int?\n  :end-row int?\n  :end-col int?}\n  ```\n\n  If `:type` is `:fn`, `:method` or `:macro`, additionally `:arglists`\n  will be added.\n\n**Location & Metadata**\n\n| Key | Value                       |\n| --- | --------------------------- |\n| Lines | 145 - 189 |\n| `:added` | 0.1 |\n| `:author` | Chad Angelelli |\n\n## get-namespace-symbol\n\nReturns namespace as symbol from result of `parse-namespace`. Automatically\n  called from `parse-namespace` for constructing AST.\n\n**Location & Metadata**\n\n| Key | Value                       |\n| --- | --------------------------- |\n| Lines | 191 - 196 |\n| `:added` | 0.1 |\n| `:author` | Chad Angelelli |\n\n## parse-namespace\n\nReturns map of `{NAMESPACE-SYMBOL PARSED}` where `PARSED` is a sequence of\n  nodes as returned by `parse-node`.\n\n**Location & Metadata**\n\n| Key | Value                       |\n| --- | --------------------------- |\n| Lines | 198 - 213 |\n| `:added` | 0.1 |\n| `:author` | Chad Angelelli |\n\n## parse-namespaces\n\nParses all namespaces for `files` by calling `parse-namespace` on each.\n  Returns vector of `[All-PARSED-NAMESPACES ?ERRORS]`.\n\n**Location & Metadata**\n\n| Key | Value                       |\n| --- | --------------------------- |\n| Lines | 215 - 227 |\n| `:added` | 0.1 |\n| `:author` | Chad Angelelli |\n\n## parse\n\nTakes a seqable of paths (as strings, files or directories) and\n  returns a map of `{:ast {...}, :errs [...], :options OPTIONS}`.\n\n**Location & Metadata**\n\n| Key | Value                       |\n| --- | --------------------------- |\n| Lines | 230 - 244 |\n| `:added` | 0.1 |\n| `:author` | Chad Angelelli |\n\n## get-namespace-node\n\nReturns namespace node for `nodes` as returned from `parse-node`.\n\n**Location & Metadata**\n\n| Key | Value                       |\n| --- | --------------------------- |\n| Lines | 246 - 250 |\n| `:added` | 0.1 |\n| `:author` | Chad Angelelli |",
                :filename "docs/docist.parser.md"})
             out))
      ))) ; end docist-parser-all-nodes-with-default-options
