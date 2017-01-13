TimeAnnotations Analysis for Elasticsearch
==================================

This project is heavily based on [the pinyin analyzer](https://github.com/medcl/elasticsearch-analysis-pinyin)

1.Create a index with custom analyzer
<pre>
curl -XPUT http://localhost:9200/test/ -d'
{
    "index" : {
        "analysis" : {
            "analyzer" : {
                "videoanalyzer" : {
                    "type" : "custom",
                    "tokenizer": "whitespace",
                    "filter": [ "lowercase", "timecode_tokenfilter"]
                    }
            }
        }
    }
}'
</pre>

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
      "analyzer": "videoanalyzer"
    },
    "shots": {
      "type": "text",
      "term_vector": "with_positions_offsets",
      "analyzer": "videoanalyzer"
    }
  }
}'
</pre>

3.Add documents
<pre>
curl -XPOST http://localhost:9200/test/video/1 -d'{"asr":"Star|12|14 Wars|14|15", "shots": "s|123|124 s|240|250"}'
curl -XPOST http://localhost:9200/test/video/2 -d'{"asr":"Hello|12|14 World|14|15", "shots": "s|123|124 s|124|213"}'
</pre>

4.Let's search
<pre>
curl http://localhost:9200/test/video/_search?q=asr:world?pretty=true
</pre>

5.Show term vector
<pre>
curl -XGET 'http://localhost:9200/test/video/1/_termvectors?pretty=true'
</pre>


6.That's all, have fun.
