{% safe %}
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

{% endsafe%}
