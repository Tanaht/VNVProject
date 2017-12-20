package fr.istic.vnv.analysis;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyInt;


public class TestBehaviorContext {

    private BehaviorContext behaviorContext;

    @BeforeAll
    static void initAll() {

    }

    @BeforeEach
    void init() {
        behaviorContext = new BehaviorContext("nameMethod(String)");
    }

    @Test
    void should_have_empty_map(){
        assertThat(behaviorContext.getLineCounters().isEmpty(),is(equalTo(true)));
    }

    @Test
    void getLineCounter_can_not_return_null(){
        assertThat(behaviorContext.getLineCounter(anyInt()),any(LineCounter.class));
    }

    @Test
    void should_not_be_empty_after_getLineCounter(){
        behaviorContext.getLineCounter(anyInt());
        assertThat(behaviorContext.getLineCounters().isEmpty(),is(not(equalTo(true))));
    }

    @Test
    void lineCounter_should_be_empty_after_getLineCounter_who_did_not_exist(){
        assertThat(behaviorContext.getLineCounters().isEmpty(), is(equalTo(true)));
        LineCounter newLineCounter = behaviorContext.getLineCounter(anyInt());
        assertThat(newLineCounter.getBlocksCounters().isEmpty(),is(equalTo(true)));
    }
}
