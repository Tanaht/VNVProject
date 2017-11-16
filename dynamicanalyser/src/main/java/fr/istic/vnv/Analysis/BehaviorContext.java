package fr.istic.vnv.Analysis;

import java.util.HashMap;
import java.util.Map;

public class BehaviorContext {

    private String longName;
    private Map<Integer, LineCounter> counters;

    public BehaviorContext(String longName) {
        this.longName = longName;
        this.counters = new HashMap<>();
    }
}
