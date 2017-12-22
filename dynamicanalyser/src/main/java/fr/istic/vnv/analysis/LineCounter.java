package fr.istic.vnv.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Class representation of the number of time a line is executed. A line can contains more than 1 block.
 * If there are more than 1 block, the line is executed only if all block of the line is executed.
 */
public class LineCounter {
    private static Logger log = LoggerFactory.getLogger(LineCounter.class);
    private int lineNumber;
    private Map<Integer, Integer> blocksCounters;

    /**
     * Representation of the line at line lineNumber
     * @param lineNumber The line number where the block start
     */
    public LineCounter(int lineNumber) {
        this.lineNumber = lineNumber;
        this.blocksCounters = new HashMap<>();
    }

    public void createCounter(int blockId) throws Exception {
        if(this.blocksCounters.containsKey(blockId)) {
            throw new Exception("For some reason a counter is being override !!!");
        }

        this.blocksCounters.put(blockId, 0);
    }

    /**
     * Increment the counter to say the block of id blockId is executed 1 more time
     * @param blockId The id of the block is executed
     */
    protected void increment(int blockId) {
        if(this.blocksCounters.containsKey(blockId)) {
            this.blocksCounters.put(blockId, this.blocksCounters.get(blockId)+1);
        } else {
            log.warn("Trying to increment an inexistant block {} on line  {}", blockId, lineNumber);
        }
    }


    /**
     * only for test
     * @return
     */
    protected int getLineNumber() {
        return lineNumber;
    }

    /**
     * only for test
     * @return
     */
    protected Map<Integer, Integer> getBlocksCounters() {
        return blocksCounters;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Line: ");
        builder.append(lineNumber);
        builder.append("\n");

        boolean isTrue = true;
        for(Integer block : this.blocksCounters.keySet()) {
            builder.append("\tCounter ");
            builder.append(block);
            builder.append(": ");
            builder.append(this.blocksCounters.get(block));
            builder.append("\n");

            //Here we see if all branch of this line has been executed
            isTrue = isTrue && (this.blocksCounters.get(block) > 0);
        }

        if(isTrue)
            builder.append("All Branch Covered at least one time\n");
        else
            builder.append("Almost one branch has not been executed\n");

        return builder.toString();
    }
}
