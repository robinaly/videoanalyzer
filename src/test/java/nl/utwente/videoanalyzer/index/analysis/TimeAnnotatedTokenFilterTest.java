package nl.utwente.videoanalyzer.index.analysis;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TimeAnnotatedTokenFilterTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

//	@Test
//	public void test() {
//		TimeAnnotatedTokenFilterFactory.parse("test|12|343");
//	}

	private class TestAnalyzer extends Analyzer {

		@Override
		protected TokenStreamComponents createComponents(String fieldName) {
			 Tokenizer tokenizer = new WhitespaceTokenizer();			 
	         TokenStream filter = new TimeAnnotatedTokenFilterFactory.TimeAnnotatedTokenFilter(tokenizer);	         
	         return new TokenStreamComponents(tokenizer, filter);
		}
		
	}
	
	@Test
	public void testFilter() throws IOException {
		
			@SuppressWarnings("resource")
			Analyzer analyzer = new TestAnalyzer();
	       
	        TokenStream stream  = analyzer.tokenStream("test", new StringReader("test|12|343|3.4 test|12|23 xzy|3|342 test2"));
	        stream.reset();
	        CharTermAttribute termAtt = stream.getAttribute(CharTermAttribute.class);
	        OffsetAttribute offsetAtt = stream.getAttribute(OffsetAttribute.class);
	        PayloadAttribute payAtt = stream.getAttribute(PayloadAttribute.class);
        
            while(stream.incrementToken()) {
            	if (payAtt.getPayload() != null) {
            		ByteBuffer buf = ByteBuffer.wrap(payAtt.getPayload().bytes);
            		System.out.println("BUF " + new String(termAtt.buffer(), 0, termAtt.length()) + " " + offsetAtt.startOffset() + " " + offsetAtt.endOffset() + " " + buf.getFloat());
            	} else {
            		System.out.println("BUF " + new String(termAtt.buffer(), 0, termAtt.length()) + " " + offsetAtt.startOffset() + " " + offsetAtt.endOffset());
            	}
            }
        


	    
	}
}
