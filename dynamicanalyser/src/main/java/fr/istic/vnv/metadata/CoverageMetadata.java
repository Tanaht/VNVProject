package fr.istic.vnv.metadata;

import java.util.ArrayList;
import java.util.List;

public class CoverageMetadata {
    private static CoverageMetadata instance;

    private static CoverageMetadata get() {
        if(instance == null)
            instance = new CoverageMetadata();

        return instance;
    }

    private List<String> executionTrace;
    // private Map<String, ?> ClassMetadataByIdentifier;

    private CoverageMetadata() {
        this.executionTrace = new ArrayList<>();
    }

    public void execute(String className, int lineNumber, String methodName) {
        get().executionTrace.add("[" + lineNumber + "]" + className + " " + methodName);
    }


}
