package fr.istic.vnv.Analysis;

import java.util.ArrayList;
import java.util.List;

public class AnalysisContext {
    private static AnalysisContext analysisContext;

    public static AnalysisContext getAnalysisContext() {
        if(analysisContext == null)
            analysisContext = new AnalysisContext();

        return analysisContext;
    }

    private AnalysisContext() {
        executionTrace = new ArrayList<>();
    }

    private List<String> executionTrace;

    public void addExecutionTrace(String trace) {
        this.executionTrace.add(trace);
    }

    public List<String> getExecutionTrace() {
        return this.executionTrace;
    }
}
