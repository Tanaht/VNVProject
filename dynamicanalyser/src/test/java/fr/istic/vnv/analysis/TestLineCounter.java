package fr.istic.vnv.analysis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestLineCounter {

    LineCounter lineCounter;

    @BeforeAll
    static void initAll() {

    }

    @BeforeEach
    void init() {
        lineCounter = new LineCounter(0);
    }

    @Test
    void should_have_empty_map(){
        assertThat(lineCounter.getBlocksCounters().isEmpty(),is(equalTo(true)));
    }

    @Test
    void should_have_line_number_to_zero(){
        assertThat(lineCounter.getLineNumber(),is(equalTo(0)));
    }

    @Test
    void should_not_be_empty(){
        lineCounter.createCounter(1);
        assertThat(lineCounter.getBlocksCounters().isEmpty(),is(not(equalTo(true))));
    }

    @Test
    void should_contain_correct_blockId(){
        lineCounter.createCounter(1);
        lineCounter.createCounter(2);

        assertThat(lineCounter.getBlocksCounters().keySet(),containsInAnyOrder(1,2));
    }


    @Test
    void should_have_zero_execution_time(){
        int blockId = 1;
        lineCounter.createCounter(blockId);

        assertThat(lineCounter.getBlocksCounters().get(blockId),is(equalTo(0)));
    }

    @Test
    void should_have_one_in_execution_time(){
        int blockId = 1;
        lineCounter.createCounter(blockId);
        lineCounter.increment(blockId);

        assertThat(lineCounter.getBlocksCounters().get(blockId),is(equalTo(1)));
    }

}
