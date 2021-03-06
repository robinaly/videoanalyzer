TimeAnnotations Analysis for Elasticsearch
==================================

This project is heavily based on [the pinyin analyzer](https://github.com/medcl/elasticsearch-analysis-pinyin)

Installation
=============

Install the plugin:

<pre>
elasticsearch-plugin install file:///$PWD/target/releases/elasticsearch-videoAnalysis-5.1.1.zip
</pre>

Tests
============

1.Create a index with custom analyzer
<pre>
curl -XPUT http://localhost:9200/test/ -d'
{
    "settings" : {
        "analysis" : {
            "analyzer" : {
                "videoanalyzer" : {
                    "type" : "custom",
                    "tokenizer": "whitespace",
                    "filter": [ "lowercase", "timecode_tokenfilter"]
                }
            }
        }
    },
    "mappings": {
      "video": {
        "properties": {
          "asr": {
            "type": "text",
            "term_vector": "with_positions_offsets_payloads",
            "index_options": "positions",
            "analyzer": "videoanalyzer"
          },
          "shots": {
            "type": "text",
            "term_vector": "with_positions_offsets",
            "index_options": "positions",
            "analyzer": "videoanalyzer"
          }
        }
      }
    }
}'
</pre>

curl -XPOST http://localhost:9200/test/_analyze -d'
{
  "analyzer": "videoanalyzer",
  "text": "abc|123|123 hello|123|110"
}'


2.Create mapping for videos: two fields: asr and shots)

If you want to store confidences, choose for "term_vector": "with_positions_offsets_payloads" otherwise 
"with_positions_offsets".

<pre>
curl -XPOST http://localhost:9200/test/_mapping/video -d'
{
  "properties": {
    "asr": {
      "type": "text",
      "term_vector": "with_positions_offsets_payloads",
      "index_options": "positions",
      "analyzer": "videoanalyzer"
    },
    "shots": {
      "type": "text",
      "term_vector": "with_positions_offsets",
      "index_options": "positions",
      "analyzer": "videoanalyzer"
    }
  }
}'
</pre>

3.Add documents
<pre>
curl -XPOST http://localhost:9200/test/video/1 -d'{"asr":"Star|12|14 Wars|23|29", "shots": "s|123|124 s|240|250"}'
curl -XPOST http://localhost:9200/test/video/2 -d'{"asr":"Hello|12|14 World|14|15", "shots": "s|123|124 s|124|213"}'
</pre>

4.Let's search
<pre>
curl 'http://localhost:9200/test/video/_search?q=asr:World&pretty=true'
</pre>

Version that only returns the shots:
<pre>
curl http://localhost:9200/test/video/_search?pretty=true -d '
{
  "_source" : ["shots", "asr"],
  "query": {
    "query_string": {
      "query": "asr:hello"
    }
  }
}'
</pre>

<pre>
curl http://localhost:9200/test/video/_search?pretty=true -d '
{
  "query": {
    "query_string": {
      "query": "hello"
    }
  },
  "script_fields" : {
      "test" : {
        "script" : {
          "inline" : "show_findings",
          "lang" : "native",
          "params" : {
            "field" : "content",
            "terms" : [
              "test",
              "hello"
            ]
          }
        },
        "ignore_failure" : false
      }
    }
}'
</pre>


5.Show term vector
<pre>
curl -XGET 'http://localhost:9200/test/video/1/_termvectors?pretty=true'
</pre>


6.That's all, have fun.


Demo data

import loremipsum
import json
import random
words = ' '.join(get_sentences(1000))
last=0
res = []
for word in words.split():
  start = last + random.randint(1,5)
  end = start + random.randint(1,10)
  last = end
  res.append('%s|%d|%d' % (word,start,end))

shots = []
for i in range(50):
  start = last + random.randint(0, 5)
  end = start + random.randint(10, 15)
  last = end 
  shots.append('%s|%d|%d' % ('s',start,end))

print len(words.split())
l = len(words)
l2 = len(' '.join(res))
print l, l2, l2 * 1.0 / l

doc = {"asr": ' '.join(res), "shots": ' '.join(shots)}

with open('test.json', 'w') as f:
  json.dump(doc, f, indent=2)