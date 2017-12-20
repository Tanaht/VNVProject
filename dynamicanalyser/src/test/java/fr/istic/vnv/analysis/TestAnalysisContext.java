package fr.istic.vnv.analysis;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class TestAnalysisContext {

    private AnalysisContext analysisContext;

    @BeforeAll
    static void initAll() {

    }

    @BeforeEach
    void init() {
        analysisContext = AnalysisContext.getAnalysisContext();
    }

}
