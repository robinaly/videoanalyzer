package nl.utwente.videoanalyzer.index.analysis;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.elasticsearch.common.logging.Loggers;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;


public class TimeAnnotatedTokenFilterFactory extends AbstractTokenFilterFactory {
	public final static String NAME = "timecode_tokenfilter";

    public TimeAnnotatedTokenFilterFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, name, settings);
        System.out.println("Test");
        Loggers.getLogger(TimeAnnotatedTokenFilterFactory.class).warn("Initialized");
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
    	Loggers.getLogger(TimeAnnotatedTokenFilterFactory.class).warn("Get filter");
        return new TimeAnnotatedTokenFilter(tokenStream);
    }
    
    public static final class TimeAnnotatedTokenFilter extends TokenFilter {
    	private final char delimiter = '|';
		private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    	private final PayloadAttribute payAtt   = addAttribute(PayloadAttribute.class);
    	
    	protected TimeAnnotatedTokenFilter(TokenStream input) {
			super(input);
			Loggers.getLogger(TimeAnnotatedTokenFilterFactory.class).warn("Started");
		}
 
    	//
		@Override
		public boolean incrementToken() throws IOException {
		    if (input.incrementToken()) {
		        final char[] buffer = termAtt.buffer();
		        final int length = termAtt.length();
		        int fieldNo = 0;
		        int iFirst = 0;
		        int iSecond = 0;
		        int iThird = 0;
		        for (int i = 0; i < length; i++) {
		          if (buffer[i] == delimiter) {		        	
		            if (fieldNo == 0) {
		            	iFirst = i;
		            } else if (fieldNo == 1) {
		            	iSecond = i;
		            } else if (fieldNo == 2) {
		            	iThird = i;
		            } 
		            fieldNo += 1;
		          }
		        }
		        termAtt.setLength(iFirst > 0 ? iFirst : length); // simply set a new length
		        Loggers.getLogger(TimeAnnotatedTokenFilterFactory.class).warn(new String(termAtt.buffer(), 0, termAtt.length()));
		        if (fieldNo >= 2) {
		        	int len = iSecond - iFirst - 1;
		        	int start = Integer.parseInt(new String(buffer, iFirst+1, len));
		        	len = ( iThird > 0 ? iThird : length ) - iSecond - 1;
		        	int end = Integer.parseInt(new String(buffer, iSecond+1, len));
		        	offsetAtt.setOffset(start, end);
		        	System.err.println(termAtt.length() + " " + start + " " + end);
		        }
		        if (fieldNo >= 3) {
		        	int len = length - iThird - 1;
		        	float conf = Float.parseFloat(new String(buffer, iThird+1, len));
		        	ByteBuffer buf = ByteBuffer.allocate(Float.BYTES);
		        	buf.putFloat(conf);
		        	payAtt.setPayload(new BytesRef(buf.array()));
		        }
		        return true;
		      } else return false;
		}
		
		@Override
		public void reset() throws IOException {
			super.reset();
		}
    }

}
