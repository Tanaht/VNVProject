package fr.istic.vnv.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AnalysisContext {
    private static Logger log = LoggerFactory.getLogger(AnalysisContext.class);

    private static AnalysisContext analysisContext;

    public static AnalysisContext getAnalysisContext() {
        if(analysisContext == null)
            analysisContext = new AnalysisContext();

        return analysisContext;
    }

    /**
     * report A Branch Coverage Counter, a Branch Coverage counter is set on a method (or constructor)
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

    public static void createBranchCoverage(String className, String methodName, int block, int lineNumber) {
        try {
            AnalysisContext context = getAnalysisContext();

            ClassContext classContext = context.getClassContext(className);
            BehaviorContext behaviorContext = classContext.getBehaviorContext(methodName);

            //This line create the appropriate lineCounter instance.
            behaviorContext.getLineCounter(lineNumber).createCounter(block);

            log.trace("At {} {} Create counter of block {} at line {}", className, methodName, block, lineNumber);
        } catch (Exception e) {
            // FIXME: This error is ignored because it appears to not cause major problems even if it is trigerred many times
            log.trace("At {} {} Try to recreate counter of block {} at line {}", className, methodName, block, lineNumber);

//            if(log.isDebugEnabled())
//                e.printStackTrace(App.syserr);
        }
    }

    private List<String> executionTrace;
    private Map<String, ClassContext> classContexts;
    private int maxExecutionTraceDepth, currentExecutionTraceDepth;
    private List<String> instrumentedMethods;

    public void resetCurrentExecutionTraceDepth() {
        this.currentExecutionTraceDepth = 0;
    }

    private AnalysisContext() {
        this.maxExecutionTraceDepth = 3;
        this.currentExecutionTraceDepth = 0;
        this.executionTrace = new ArrayList<>();
        this.classContexts = new HashMap<>();
        this.instrumentedMethods = new ArrayList<>();
    }

    public void addExecutionTrace(String message) {
        getAnalysisContext().executionTrace.add(message);
    }
    /**
     * Called By Javassisted methods at starts of each of them.
     * @param trace message to record
     * @param args arguments list of javassisted method.
     */
    public static void addStartExecutionTrace(String trace, Object... args) {

        if(++getAnalysisContext().currentExecutionTraceDepth > getAnalysisContext().maxExecutionTraceDepth)
            return;

        StringBuilder parametersBuilder = new StringBuilder(trace).append("(");

        for (int i = 0; i < args.length; i++) {
            if(args[i] == null) {
                parametersBuilder.append("null, ");
                continue;
            }

            if(args[i].getClass().isPrimitive()) {
                parametersBuilder.append(args[i].toString()).append(", ");
                continue;
            }

            if(args[i] instanceof String) {
                if (((String) args[i]).length() > 15) {
                    parametersBuilder.append(((String) args[i]).substring(0, 15)).append("..., ");
                } else {
                    parametersBuilder.append((String) args[i]).append(", ");
                }
                continue;
            }

            parametersBuilder.append(System.identityHashCode(args[i])).append(", ");


        }
        parametersBuilder.append(")");

        if(getAnalysisContext().executionTrace.indexOf(parametersBuilder.toString()) != -1) {
            getAnalysisContext().addExecutionTrace(trace);
        } else {
            getAnalysisContext().addExecutionTrace(parametersBuilder.toString());
        }
    }

    public boolean isInstrumented(String methodDescriptor) {
        return this.instrumentedMethods.contains(methodDescriptor);
    }

    public void instrument(String methodDescriptor) {
        this.instrumentedMethods.add(methodDescriptor);
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
