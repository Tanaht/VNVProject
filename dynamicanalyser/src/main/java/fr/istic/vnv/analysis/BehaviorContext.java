package fr.istic.vnv.analysis;

import java.util.HashMap;
import java.util.Map;

public class BehaviorContext {

    private String name;
    private Map<Integer, LineCounter> lineCounters;

    public BehaviorContext(String name) {
        this.name = name;
        this.lineCounters = new HashMap<>();
    }

    public LineCounter getLineCounter(int lineNumber) {
        if(this.lineCounters.containsKey(lineNumber))
            return this.lineCounters.get(lineNumber);

        LineCounter lineCounter = new LineCounter(lineNumber);
        this.lineCounters.put(lineNumber, lineCounter);
        return lineCounter;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("###");
        builder.append(this.name);
        builder.append(":\n");

        for(Integer lineNumber : this.lineCounters.keySet()) {
            builder.append(this.lineCounters.get(lineNumber));
        }

        return builder.toString();
    }
}
