package nl.utwente.videoanalyzer.index.analysis;

import static org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner.newConfigs;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import nl.utwente.videoanalyzer.plugin.VideoAnalyzerPlugin;
import nl.utwente.videoanalyzer.script.Findings;

import org.apache.lucene.queryparser.xml.QueryBuilderFactory;
import org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.termvectors.TermVectorsRequestBuilder;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.common.lucene.search.function.ScriptScoreFunction;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
@ThreadLeakScope(ThreadLeakScope.Scope.NONE)
@RunWith(com.carrotsearch.randomizedtesting.RandomizedRunner.class)
public class TimeAnnotatedTokenFilterFactoryTest  {
    private ElasticsearchClusterRunner runner;

    private String clusterName;
    
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
	}

	@Before
	public void setUp() throws Exception {
		clusterName = "es-cl-run-" + System.currentTimeMillis();
        // create runner instance
        runner = new ElasticsearchClusterRunner();
        // create ES nodes
        runner.onBuild(new ElasticsearchClusterRunner.Builder() {
            @Override
            public void build(int number, Builder settingsBuilder) { 
            }
        }).build(
                newConfigs()
                .pluginTypes(VideoAnalyzerPlugin.class.getName())
                .clusterName(clusterName)
                .numOfNode(1));

        // wait for yellow status
        runner.ensureYellow();
	}

	@After
	public void tearDown() throws Exception {
	       // close runner
        runner.close();
        // delete all files
        runner.clean();
	}

//	@Test
//	public void test() throws IOException {
//		final String index = "test_index";
//		
//		Settings indexSettings = Settings.builder()
//				.put("analysis.analyzer.videoanalyzer.type", "custom")
//                .put("analysis.analyzer.videoanalyzer.tokenizer", "whitespace")
//                .put("analysis.analyzer.videoanalyzer.filter.0", "lowercase")
//                .put("analysis.analyzer.videoanalyzer.filter.1", TimeAnnotatedTokenFilterFactory.NAME)
//				.build();
//        CreateIndexResponse request = runner.createIndex(index, indexSettings);
//        
//        XContentBuilder mapping = XContentFactory.jsonBuilder().startObject()
//                .startObject("doc")
//                .startObject("properties")
//                .startObject("content")
//                    .field("type", "text")
//                    .field("analyzer", "videoanalyzer" )
//                    .field("term_vector", "with_positions_offsets_payloads")                    
//                .endObject()
//                .endObject()
//                .endObject()
//                .endObject();
//         
//        runner.createMapping(index, "doc", mapping);
//        
//        runner.insert(index, "doc", "1233", "{\"content\": \"test|12|123 \"}");
//        runner.ensureYellow(index);
//        TermVectorsRequestBuilder t = runner.client().prepareTermVectors(index, "doc", "1233");
//        
//        TermVectorsResponse res = t.get();
//        new XContentFactory();
//		XContentBuilder builder = XContentFactory.jsonBuilder();
//		builder.startObject();
//        res.toXContent(builder, null);
//        builder.endObject();
//        System.out.println(builder.prettyPrint().string());
//      
//        final SearchResponse searchResponse = runner.search(index, "doc",
//                null, null, 0, 10);
//        long docs = searchResponse.getHits().getTotalHits();
//        System.out.println(docs+"");
//        assert(1 == docs);
//          
//        assert true;
//        //final SearchResponse test = runner.client().prepareSearch(index).setS //setSource(addQuery()).execute().actionGet();
//	}

	
	@Test
	public void testScript() throws IOException, InterruptedException, ExecutionException {
		final String index = "test_index2";
		final String docType = "video";
		final String field = "asr";
		final String analyzer = "videoanalyzer";
		final String docid = "1111";
		
//		Settings indexSettings = Settings.builder()
//				.put("analysis.analyzer." + analyzer + ".type", "custom")
//                .put("analysis.analyzer." + analyzer + ".tokenizer", "whitespace")
//                .put("analysis.analyzer." + analyzer + ".filter.1", "lowercase")
//                .put("analysis.analyzer." + analyzer + ".filter.0", TimeAnnotatedTokenFilterFactory.NAME)
//				.build();
		
		
        
        {
        	String createIndexRequestSource = 
        			"{"+
        					"    \"settings\" : {"+
        					"        \"analysis\" : {"+
        					"            \"analyzer\" : {"+
        					"                \"videoanalyzer\" : {"+
        					"                    \"type\" : \"custom\","+
        					"                    \"tokenizer\": \"whitespace\","+
        					"                    \"filter\": [ \"lowercase\", \"" + TimeAnnotatedTokenFilterFactory.NAME + "\"]"+
        					"                }"+
        					"            }"+
        					"        }"+
        					"    },"+
        					"    \"mappings\": {"+
        					"      \"video\": {"+
        					"        \"properties\": {"+
        					"          \"asr\": {"+
        					"            \"type\": \"text\","+
        					"            \"term_vector\": \"with_positions_offsets_payloads\","+
        					"            \"index_options\": \"offsets\","+
        					"            \"analyzer\": \"videoanalyzer\""+
        					"          },"+
        					"          \"shots\": {"+
        					"            \"type\": \"text\","+
        					"            \"term_vector\": \"with_positions_offsets\","+
        					"            \"index_options\": \"offsets\","+
        					"            \"analyzer\": \"videoanalyzer\""+
        					"          }"+
        					"        }"+
        					"      }"+
        					"    }"+
        					"}";
        	ActionFuture<CreateIndexResponse> createIndexResult = runner.admin().indices().create(new CreateIndexRequest().index(index).source(createIndexRequestSource));
        	System.out.println("Creat Index: " + createIndexResult.get().isAcknowledged());
        	
        }
        	
        
        runner.ensureYellow(index);
        
        /* insert test data */
        IndexResponse indexResp= runner.insert(index, docType, docid, "{\"" + field + "\": \"test|5|10 hello|11|16\"}");
        System.out.println(indexResp.status().name());
        
        /* search all */
        final SearchResponse searchResponse = runner.search(index, docType,
                null, null, 0, 10);
        System.out.println(searchResponse.toString());
        
        Map<String, Object> params = new HashMap<String, Object>();
        List<String> terms = new LinkedList<String>();
        terms.add("test");
        terms.add("hello");
        params.put("field", field);
        params.put("terms", terms);
        SearchRequestBuilder scrollResp = runner.client().prepareSearch(index)
                .setTypes(docType)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.termQuery(field, "test")) // **<-- Query string in JSON format**
                .addScriptField("test", new Script(ScriptType.INLINE, "native", Findings.NAME, params));
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        System.err.println(scrollResp.toString());
        SearchResponse res = 
                scrollResp.execute().actionGet();
        XContentBuilder jbuilder = XContentFactory.jsonBuilder();
        jbuilder.startObject();
        res.toXContent(jbuilder, null);
        jbuilder.endObject();
        System.err.println(jbuilder.prettyPrint().string());
        
        /* get term fector */
        TermVectorsRequestBuilder t = runner.client().prepareTermVectors(index, docType, docid);
        TermVectorsResponse vec = t.get();
        
		XContentBuilder gbuilder = XContentFactory.jsonBuilder();
		gbuilder.startObject();
        vec.toXContent(gbuilder, null);
        gbuilder.endObject();
        System.out.println(gbuilder.prettyPrint().string());
//		  
        assert true;
        //final SearchResponse test = runner.client().prepareSearch(index).setS //setSource(addQuery()).execute().actionGet();
	}
}
