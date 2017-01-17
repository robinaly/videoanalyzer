package nl.utwente.videoanalyzer.plugin;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.utwente.videoanalyzer.index.analysis.TimeAnnotatedTokenFilterFactory;
import nl.utwente.videoanalyzer.script.Findings;

import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.analysis.TrimTokenFilterFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.indices.analysis.AnalysisModule.AnalysisProvider;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.ScriptPlugin;
import org.elasticsearch.script.NativeScriptFactory;


public class VideoAnalyzerPlugin extends Plugin implements AnalysisPlugin, ScriptPlugin {

	public VideoAnalyzerPlugin() {
		Loggers.getLogger(VideoAnalyzerPlugin.class).warn("Inside Analyzer");
	} 
	
    @Override
        public Map<String, AnalysisModule.AnalysisProvider<org.elasticsearch.index.analysis.TokenFilterFactory>> getTokenFilters() {
    	        Map<String, AnalysisModule.AnalysisProvider<org.elasticsearch.index.analysis.TokenFilterFactory>> extra = new HashMap<>();
    	        //extra.put("timecode_tokenfilter", TimeAnnotatedTokenFilterFactory::new);
        extra.put(TimeAnnotatedTokenFilterFactory.NAME, TimeAnnotatedTokenFilterFactory::new);
        Loggers.getLogger(VideoAnalyzerPlugin.class).warn("getTokenFilter");
        return extra;
    }
    
    
    @Override
    public List<NativeScriptFactory> getNativeScripts() {
    	Settings settings = Settings.builder().build();
        return Collections.singletonList(new Findings.Factory(settings));
    }
    
}
