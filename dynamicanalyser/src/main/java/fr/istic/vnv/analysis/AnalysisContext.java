package fr.istic.vnv.analysis;

import fr.istic.vnv.instrumentation.BehaviorInstrumenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 */
public class AnalysisContext {
    private static Logger log = LoggerFactory.getLogger(AnalysisContext.class);

    private static AnalysisContext analysisContext;

    /**
     * Get the only instance of AnalysisContext possible
     * @return Instance of AnalysisContext
     */
    public static AnalysisContext getAnalysisContext() {
        if(analysisContext == null)
            analysisContext = new AnalysisContext();

        return analysisContext;
    }

    /**
     * Report A Branch Coverage Counter, a Branch Coverage counter is set on a method (or constructor)
     * and on a specific line number on a given block.
     * a block is a set of instruction that has no jump inside it (so it is executed with no loop).
     * But a block can jump to another block. So this is why we report Branch Coverage Counter based on blocks.
     * @param className the class name of the input project method
     * @param methodName the method where the counter is set
     * @param block the block index of the counter
     * @param lineNumber the line number of the counter
     */
    public static void reportBranchCoverage(String className, String methodName, int block, int lineNumber) {
        AnalysisContext context = getAnalysisContext();

        ClassContext classContext = context.getClassContext(className);
        BehaviorContext behaviorContext = classContext.getBehaviorContext(methodName);
        behaviorContext.getLineCounter(lineNumber).increment(block);

        log.trace("At {} {} Increment counter of block {} at line {}", className, methodName, block, lineNumber);
    }

    /**
     * creating a counter for a method class in a specific block of the method
     * @param className The name of the class where there is the method
     * @param methodName The name of the method where you want to create a counter block
     * @param block The number of the specific block where you want to associate a counter
     * @param lineNumber The line number in the source code where the block start
     */
    public static void createBranchCoverage(String className, String methodName, int block, int lineNumber) {
        AnalysisContext context = getAnalysisContext();

        ClassContext classContext = context.getClassContext(className);
        BehaviorContext behaviorContext = classContext.getBehaviorContext(methodName);

        //This line create the appropriate lineCounter instance.
        behaviorContext.getLineCounter(lineNumber).createCounter(block);

        log.trace("At {} {} Create counter of block {} at line {}", className, methodName, block, lineNumber);
    }

    private List<String> executionTrace;
    private Map<String, ClassContext> classContexts;

    private AnalysisContext() {
        this.executionTrace = new ArrayList<>();
        this.classContexts = new HashMap<>();
    }

    /**
     *
     * @param trace
     */
    public void addExecutionTrace(String trace) {
        this.executionTrace.add(trace);
    }

    /**
     *
     * @return
     */
    public List<String> getExecutionTrace() {
        return this.executionTrace;
    }


    /**
     * Retrieve ClassContext, if not found, create it.
     * @param classname
     * @return
     */
    public ClassContext getClassContext(String classname) {
        Optional<ClassContext> classContextOptional = Optional.ofNullable(this.classContexts.get(classname));

        if(classContextOptional.isPresent())
            return classContextOptional.get();

        ClassContext classContext = new ClassContext(classname);
        this.classContexts.put(classname, classContext);
        return classContext;
    }

    /**
     *
     * @return
     */
    public Map<String, ClassContext> getClassContexts() {
        return this.classContexts;
    }
}
