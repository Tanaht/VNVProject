package fr.istic.vnv.analysis;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;

public class TestClassContext {
    private ClassContext classContext;

    @BeforeAll
    static void initAll() {

    }

    @BeforeEach
    void init() {
        classContext = new ClassContext("fr.istic.vnv.test");
    }

    @Test
    void getBehaviorContext_can_not_return_null(){
        assertThat(classContext.getBehaviorContext(anyString()),any(BehaviorContext.class));
    }

    @Test
    void should_not_be_empty_after_getBehaviorContext(){
        classContext.getBehaviorContext(anyString());
        assertThat(classContext.getBehaviorContexts().isEmpty(),is(not(equalTo(true))));
    }

    @Test
    void behaviorContext_should_be_empty_after_getBehaviorContexts_who_did_not_exist(){
        assertThat(classContext.getBehaviorContexts().isEmpty(), is(equalTo(true)));
        BehaviorContext newBehaviorContext = classContext.getBehaviorContext(anyString());
        assertThat(newBehaviorContext.getLineCounters().isEmpty(), is(equalTo(true)));
    }
}
