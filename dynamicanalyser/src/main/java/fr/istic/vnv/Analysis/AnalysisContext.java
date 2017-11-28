package fr.istic.vnv.analysis;

import java.util.*;

public class AnalysisContext {
    private static AnalysisContext analysisContext;

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

    }

    public static void createBranchCoverage(String className, String methodName, int block, int lineNumber) {
        AnalysisContext context = getAnalysisContext();

        ClassContext classContext = context.getClassContext(className);
        BehaviorContext behaviorContext = classContext.getBehaviorContext(methodName);

        //This line create the appropriate lineCounter instance.
        behaviorContext.getLineCounter(lineNumber).createCounter(block);

    }

    private List<String> executionTrace;
    private Map<String, ClassContext> classContexts;

    private AnalysisContext() {
        this.executionTrace = new ArrayList<>();
        this.classContexts = new HashMap<>();
    }

    public void addExecutionTrace(String trace) {
        this.executionTrace.add(trace);
    }

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

    public Map<String, ClassContext> getClassContexts() {
        return this.classContexts;
    }
}
