package fr.istic.vnv.analysis;

import java.util.HashMap;
import java.util.Map;

/**
 * Class representation to see what is it executed in a method.
 */
public class BehaviorContext {

    private String name;
    private Map<Integer, LineCounter> lineCounters;

    /**
     *
     * @param name The name of a method and the descriptor of the method.
     */
    public BehaviorContext(String name) {
        this.name = name;
        this.lineCounters = new HashMap<>();
    }

    /**
     * Get the LineCounter of the lineNumber.
     * @param lineNumber The lineNumber you want to get the LineCounter
     * @return The line counter of the line lineNumber
     */
    public LineCounter getLineCounter(int lineNumber) {
        if(this.lineCounters.containsKey(lineNumber))
            return this.lineCounters.get(lineNumber);

        LineCounter lineCounter = new LineCounter(lineNumber);
        this.lineCounters.put(lineNumber, lineCounter);
        return lineCounter;
    }

    /**
     * only for test
     * @return
     */
    protected String getName() {
        return name;
    }
    /**
     * only for test
     * @return
     */
    protected Map<Integer, LineCounter> getLineCounters() {
        return lineCounters;
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
