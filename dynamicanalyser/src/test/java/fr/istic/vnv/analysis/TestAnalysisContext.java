package fr.istic.vnv.analysis;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;

public class TestAnalysisContext {

    private AnalysisContext analysisContext;

    @BeforeAll
    static void initAll() {

    }

    @BeforeEach
    void init() {
        analysisContext = AnalysisContext.getResetSingleton();
    }

    @Test
    void should_have_empty_map(){
        assertThat(analysisContext.getClassContexts().isEmpty(),is(equalTo(true)));
    }

    @Test
    void getClassContext_can_not_return_null(){
        assertThat(analysisContext.getClassContext(anyString()),any(ClassContext.class));
    }

    @Test
    void should_not_be_empty_after_getClassContext(){
        analysisContext.getClassContext(anyString());
        assertThat(analysisContext.getClassContexts().isEmpty(),is(not(equalTo(true))));
    }

    @Test
    void classContext_should_be_empty_after_getClassContexts_who_did_not_exist(){
        assertThat(analysisContext.getClassContexts().isEmpty(), is(equalTo(true)));
        ClassContext newClassContext = analysisContext.getClassContext(anyString());
        assertThat(newClassContext.getBehaviorContexts().isEmpty(),is(equalTo(true)));
    }

}
