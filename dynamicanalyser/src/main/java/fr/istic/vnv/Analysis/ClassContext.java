package fr.istic.vnv.Analysis;

import java.util.HashMap;
import java.util.Map;

public class ClassContext {
    private String longName;
    private Map<String, BehaviorContext> behaviorContexts;

    public ClassContext(String longName) {
        this.longName = longName;
        this.behaviorContexts = new HashMap<>();
    }
}
