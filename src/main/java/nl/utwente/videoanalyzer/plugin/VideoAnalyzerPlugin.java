package nl.utwente.videoanalyzer.plugin;


import java.util.HashMap;
import java.util.Map;

import nl.utwente.videoanalyzer.index.analysis.TimeAnnotatedTokenFilterFactory;

import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;


public class VideoAnalyzerPlugin extends Plugin implements AnalysisPlugin {

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<org.elasticsearch.index.analysis.TokenFilterFactory>> getTokenFilters() {
        Map<String, AnalysisModule.AnalysisProvider<org.elasticsearch.index.analysis.TokenFilterFactory>> extra = new HashMap<>();
        extra.put("timecode_tokenfilter", TimeAnnotatedTokenFilterFactory::new);
        return extra;
    }
}
