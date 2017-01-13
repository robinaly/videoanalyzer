package nl.utwente.videoanalyzer.index.analysis;

import static org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner.newConfigs;

import java.io.IOException;

import nl.utwente.videoanalyzer.plugin.VideoAnalyzerPlugin;

import org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.termvectors.TermVectorsRequestBuilder;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
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

	@Test
	public void test() throws IOException {
		final String index = "test_index";
		
		Settings indexSettings = Settings.builder()
				.put("analysis.analyzer.videoanalyzer.type", "custom")
                .put("analysis.analyzer.videoanalyzer.tokenizer", "whitespace")
                .put("analysis.analyzer.videoanalyzer.filter.0", "lowercase")
                .put("analysis.analyzer.videoanalyzer.filter.1", "timecode_tokenfilter")
				.build();
        CreateIndexResponse request = runner.createIndex(index, indexSettings);
        
        XContentBuilder mapping = XContentFactory.jsonBuilder().startObject()
                .startObject("doc")
                .startObject("properties")
                .startObject("content")
                    .field("type", "text")
                    .field("analyzer", "videoanalyzer" )
                    .field("term_vector", "with_positions_offsets_payloads")                    
                .endObject()
                .endObject()
                .endObject()
                .endObject();
         
        runner.createMapping(index, "doc", mapping);
        runner.insert(index, "doc", "1233", "{\"content\": \"test|12|123\"}");
        runner.ensureYellow(index);
        TermVectorsRequestBuilder t = runner.client().prepareTermVectors(index, "doc", "1233");
        
        TermVectorsResponse res = t.get();
        new XContentFactory();
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject();
        res.toXContent(builder, null);
        builder.endObject();
        System.out.println(builder.prettyPrint().string());
      
        final SearchResponse searchResponse = runner.search(index, "doc",
                null, null, 0, 10);
        long docs = searchResponse.getHits().getTotalHits();
        System.out.println(docs+"");
        assert(1 == docs);
          
        assert true;
        //final SearchResponse test = runner.client().prepareSearch(index).setS //setSource(addQuery()).execute().actionGet();
	}

}
