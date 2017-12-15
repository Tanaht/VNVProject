package fr.istic.vnv.utils;

public class Config {
    private static Config config;

    public static Config get() {
        if(config == null)
            config = new Config();

        return config;
    }

    private boolean doExecutionTrace, doBranchCoverage, doOutputRedirection;

    private Config() {
        String executionTrace = System.getProperty("instrumentation.execution_trace");
        String branchCoverage = System.getProperty("instrumentation.branch_coverage");
        String redirectOutput = System.getProperty("log.redirect_output");


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
}
