package nl.utwente.videoanalyzer.index.analysis;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.payloads.DelimitedPayloadTokenFilter;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.apache.lucene.analysis.payloads.PayloadEncoder;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.util.BytesRef;


public class TimeAnnotatedTokenFilterFactory extends AbstractTokenFilterFactory {

    private final PayloadEncoder encoder;

    private final char delimiter;

    public TimeAnnotatedTokenFilterFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, name, settings);
        this.encoder = createEncoder(settings.get("encoder", "float"));
        this.delimiter = settings.get("delimiter", "|").charAt(0);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new TimeAnnotatedTokenFilter(tokenStream);
    }

    private PayloadEncoder createEncoder(String encoder) {
        return null;
    }
    
    public static final class TimeAnnotatedTokenFilter extends TokenFilter {
    	private final char delimiter = '|';
		private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    	private final PayloadAttribute payAtt = addAttribute(PayloadAttribute.class);
    	
    	protected TimeAnnotatedTokenFilter(TokenStream input) {
			super(input);
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
		        termAtt.setLength(iFirst); // simply set a new length
		        if (fieldNo >= 2) {
		        	int len = iSecond - iFirst - 1;
		        	int start = Integer.parseInt(new String(buffer, iFirst+1, len));
		        	len = ( iThird > 0 ? iThird : length ) - iSecond - 1;
		        	int end = Integer.parseInt(new String(buffer, iSecond+1, len));
		        	offsetAtt.setOffset(start, end);
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
    
    public static boolean parse(String input) {
    	char[] buffer = input.toCharArray();
    	char delimiter = '|';
    	final int length = buffer.length;
        int fieldNo = 0;
        int iFirst = 0;
        int iSecond = 0;
        int len = length;
        for (int i = 0; i < length; i++) {
          if (buffer[i] == delimiter) {		        	
            if (fieldNo == 0) {
            	iFirst = i;
            } else if (fieldNo == 1) {
            	iSecond = i;
            } 
            fieldNo += 1;
          }
        }
        System.out.println(new String(buffer, 0, len));
        System.out.println(input + " " + iFirst + " " + iSecond + " " + buffer.length);
        if (fieldNo == 2) {
        	int len1 = iSecond - iFirst - 1;
        	int len2 = length - iSecond - 1;
        	System.out.println(len1 + " " + len2);
        	int start = Integer.parseInt(new String(buffer, iFirst+1, len1));
        	int end = Integer.parseInt(new String(buffer, iSecond+1, len2));
        	System.out.println(start + " " + end);
        }
        return true;
    }
}
