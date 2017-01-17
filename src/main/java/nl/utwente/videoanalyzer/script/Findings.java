package nl.utwente.videoanalyzer.script;

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.Fields;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.script.AbstractSearchScript;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;
import org.elasticsearch.script.ScriptException;
import org.elasticsearch.search.lookup.IndexField;
import org.elasticsearch.search.lookup.IndexLookup;
import org.elasticsearch.search.lookup.TermPosition;

/**
 */
public class Findings extends AbstractSearchScript {
	public static final String NAME = "show_findings";
    /**
     * Native scripts are build using factories that are registered in the
     * {@link org.elasticsearch.examples.nativescript.plugin.NativeScriptExamplesPlugin#onModule(org.elasticsearch.script.ScriptModule)}
     * method when plugin is loaded.
     */
    public static class Factory extends AbstractComponent implements NativeScriptFactory {

        /**
         * This constructor will be called by guice during initialization
         *
         * @param node injecting the reference to current node to get access to node's client
         * @param settings current node settings
         */
        @Inject
        public Factory(Settings settings) {
            super(settings);
            // Node is not fully initialized here
            // All we can do is save a reference to it for future use

        }

        /**
         * This method is called for every search on every shard.
         *
         * @param params list of script parameters passed with the query
         * @return new native script
         */
        @SuppressWarnings("unchecked")
		@Override
        public ExecutableScript newScript(@Nullable Map<String, Object> params) {
//            if (params == null) {
//                throw new ScriptException("Missing script parameters");
//            }
//            String lookupIndex = XContentMapValues.nodeStringValue(params.get("lookup_index"), null);
//            if (lookupIndex == null) {
//                throw new ScriptException("Missing the index parameter");
//            }
//            String lookupType = XContentMapValues.nodeStringValue(params.get("lookup_type"), null);
//            if (lookupType == null) {
//                throw new ScriptException("Missing the index parameter");
//            }
        	List<String> terms = (List<String>) params.get("terms");
            String field = XContentMapValues.nodeStringValue(params.get("field"), null);
            if (field == null) {
                throw new ScriptException("Missing the field parameter", 
                		new IllegalArgumentException("Missing field"), Collections.EMPTY_LIST, getName(), "native");
            }
            return new Findings(field, terms);
        }

        /**
         * Indicates if document scores may be needed by the produced scripts.
         *
         * @return {@code true} if scores are needed.
         */
        @Override
        public boolean needsScores() {
            return false;
        }

		@Override
		public String getName() {
			return NAME;
		}

    }

    private final String field;
   
    private final Client client;
	private List<String> terms;

    private static final Map<String, Object> EMPTY_MAP = Collections.emptyMap();

    private Findings(String field, List<String> terms) {
        this.client = null;        
        this.field = field;
        this.terms = terms;
    }

    @Override
    public Object run() {
    	IndexField indexField = this.indexLookup().get(field);
    	try {
			Fields x = this.indexLookup().termVectors();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	Map<String, Object> res = new HashMap<String, Object>();
    	for (String term: terms) {
    		List<Integer> starts = new LinkedList<Integer>();
    		List<Integer> ends = new LinkedList<Integer>();
    		Iterator<TermPosition> iter = indexField.get(term, IndexLookup.FLAG_OFFSETS).iterator();
    		while (iter.hasNext()) {
    			TermPosition pos = iter.next();
    			starts.add(pos.startOffset);
    			ends.add(pos.endOffset);
    		}
    		Map<String, Object> startsends = new HashMap<String, Object>();
    		startsends.put("starts", starts);
    		startsends.put("ends", ends);
    		res.put(term, startsends);
    	}
        return res;
    }
}