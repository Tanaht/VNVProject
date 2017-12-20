package fr.istic.vnv.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Config {
    private static Config config;
    private static Logger log = LoggerFactory.getLogger(Config.class);

    public static Config get() {
        if(config == null)
            config = new Config();

        return config;
    }

    private boolean doExecutionTrace, doBranchCoverage, doOutputRedirection;
    private int traceDepth;

    private Config() {
        String executionTrace = System.getProperty("instrumentation.execution_trace");
        String branchCoverage = System.getProperty("instrumentation.branch_coverage");
        String redirectOutput = System.getProperty("log.redirect_output");
        String executionTraceDepth = System.getProperty("instrumentation.execution_trace.depth");

        if(executionTrace == null)
            doExecutionTrace = false;
        else
            doExecutionTrace = executionTrace.equals("true");

        if(branchCoverage == null)
            doBranchCoverage = true;
        else
            doBranchCoverage = branchCoverage.equals("true");


        if(redirectOutput == null)
            doOutputRedirection = true;
        else
            doOutputRedirection = redirectOutput.equals("true");

        if(doExecutionTrace) {
            if(executionTraceDepth == null) {
                traceDepth = 3;
                return;
            }

            if(executionTraceDepth.equals("max"))
                traceDepth = -1;
            else {
                try {
                    traceDepth = Integer.parseInt(executionTraceDepth);
                } catch (Exception e) {
                    log.warn("Unable to read Execution Trace Depth from System Properties, set it to 3");
                    traceDepth = 3;
                }
            }
        }
    }


    public boolean doExecutionTrace() {
        return doExecutionTrace;
    }

    public boolean doBranchCoverage() {
        return doBranchCoverage;
    }

    public boolean doOutputRedirection() {
        return doOutputRedirection;
    }

    public int getTraceDepth() {
        return traceDepth;
    }
}
