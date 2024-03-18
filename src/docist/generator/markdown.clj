(ns docist.generator.markdown
  "Generate Markdown docs."
  {:added "0.1" :author "Chad Angelelli"}
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [docist.parser :as d]
            [docist.generator.util :as gu]
            [docist.util :as u]
            [selmer.parser :as template]))

(def ^{:added "0.1" :author "Chad Angelelli"}
  default-markdown-namespace-template
"{% safe %}
# {{ns.name}}

{{ns.doc}}

### Location & Metadata

| Key  | Value |
| ---- | ----- |
| File | {{ns.file}} |
{% if ns.meta %}
{% for k,v in ns.meta %}
| `{{k}}` | {{v}} |
{% endfor %}
{% endif %}

{% for s in symbols %}
{% if s.name %}
## {{s.name}}

{{s.doc}}

### Location & Metadata

| Key | Value                       |
| --- | --------------------------- |
| Lines | {{s.row}} - {{s.end-row}} |
{% if s.meta %}
{% for k,v in s.meta %}
| `{{k}}` | {{v}} |
{% endfor %}
{% endif %}

{% endif %}
{% endfor %}

{% endsafe%}")

(defn- -cleanup-multiple-newlines
  "Replaces 3+ newline characters with 2 newline characters."
  {:added "0.1" :author "Chad Angelelli"}
  [s]
  (string/replace s #"\n\n+" "\n\n"))

(defn- -cleanup-tables
  "Removes multiple newline characters between table rows."
  {:added "0.1" :author "Chad Angelelli"}
  [s]
  (string/replace s #"\|\n\n\|" "|\n|"))

(defn render-namespace-page
  "Produce rendered Markdown for AST nodes."
  {:added "0.1" :author "Chad Angelelli"}
  [template nodes _]
  (let [context (u/split-namespace-and-symbols nodes)]
    (-> (template/render template context)
        string/trim
        -cleanup-multiple-newlines
        -cleanup-tables)))

(defn generate-docs
  "Main Markdown generator entry point."
  {:added "0.1" :author "Chad Angelelli"}
  [ast {:keys [template] :as options}]
  (let [template (or template default-markdown-namespace-template)]
    (map (fn [[ns-sym nodes]]
           {:filename (gu/make-filename ns-sym options)
            :content (render-namespace-page template nodes options)})
         ast)))
