package fr.istic.vnv.utils;

import fr.istic.vnv.App;
import fr.istic.vnv.analysis.AnalysisContext;
import fr.istic.vnv.report.ReportGenerator;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Human understandable text listener to write logs about tests being run.
 */
public class ExtendedTextListener extends RunListener {
    private int failuresCount;
    private int testsCounter, testsFinished;
    private ReportGenerator generator;

    public ExtendedTextListener(ReportGenerator generator, int testsCounter) {
        this.testsCounter = testsCounter;
        this.generator = generator;
    }

    private static Logger log = LoggerFactory.getLogger(ExtendedTextListener.class);
    @Override
    public void testFailure(Failure failure) throws Exception {
        super.testFailure(failure);
        failuresCount++;

        log.warn("Test Failed for: {}", failure.toString());
        if(log.isDebugEnabled())
            failure.getException().printStackTrace(App.syserr);
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        super.testIgnored(description);
        log.debug("Ignore Test {} - {}", description.getClassName(), description.getMethodName());
    }

    @Override
    public void testStarted(Description description) throws Exception {
        super.testStarted(description);
        log.debug("Start Test {} - {}", description.getClassName(), description.getMethodName());

        AnalysisContext.getAnalysisContext().resetCurrentExecutionTraceDepth();
        AnalysisContext.getAnalysisContext().addExecutionTrace("[TEST] " + description.getClassName() + " - " + description.getMethodName());
    }

    @Override
    public void testFinished(Description description) throws Exception {
        super.testStarted(description);
        testsFinished++;

        if(!Config.get().doExecutionTrace())
            return;

        if(AnalysisContext.getAnalysisContext().getExecutionTrace().size() >= 150000 && log.isDebugEnabled()) {
            log.debug("Tests finished: {} / {}", testsFinished, testsCounter);
            generator.saveExecutionTraceUntilThen();
        }

        if(testsFinished % 2000 == 0 && !log.isDebugEnabled()) {
            log.info("Tests finished: {} / {}", testsFinished, testsCounter);
            generator.saveExecutionTraceUntilThen();
        }

    }

    public int getFailuresCount() {
        return failuresCount;
    }
}
