package fr.istic.vnv.Analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalysisContext {
    private static AnalysisContext analysisContext;

    public static AnalysisContext getAnalysisContext() {
        if(analysisContext == null)
            analysisContext = new AnalysisContext();

        return analysisContext;
    }

    private List<String> executionTrace;
    private Map<String, ClassContext> classContexts;

    private AnalysisContext() {
        this.executionTrace = new ArrayList<>();
        this.classContexts = new HashMap<>();
    }

    public void addExecutionTrace(String trace) {
        this.executionTrace.add(trace);
    }

    public List<String> getExecutionTrace() {
        return this.executionTrace;
    }
}
