package fr.istic.vnv.utils;

import fr.istic.vnv.analysis.ClassContext;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;

public class TestConfig {

    private Config config;

    @Test
    public void should_not_be_null(){
        assertThat(Config.get(),any(Config.class));
    }
}
