package fr.istic.vnv.utils;

import fr.istic.vnv.App;
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
    public ExtendedTextListener(int testsCounter) {
        this.testsCounter = testsCounter;
    }

    private static Logger log = LoggerFactory.getLogger(ExtendedTextListener.class);
    @Override
    public void testFailure(Failure failure) throws Exception {
        super.testFailure(failure);
        failuresCount++;

        log.warn("Test Failed for: {}", failure.toString());
        if(log.isDebugEnabled())
            failure.getException().printStackTrace(App.sysout);
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        super.testIgnored(description);
        log.trace("Ignore Test {} - {}", description.getClassName(), description.getMethodName());
    }

    @Override
    public void testFinished(Description description) throws Exception {
        super.testStarted(description);
        testsFinished++;

        if(testsFinished % 1000 == 0 && log.isDebugEnabled()) {
            log.debug("Tests finished: {} / {}", testsFinished, testsCounter);
        }

        if(testsFinished % 5000 == 0 && !log.isDebugEnabled()) {
            log.info("Tests finished: {} / {}", testsFinished, testsCounter);
        }

    }

    public int getFailuresCount() {
        return failuresCount;
    }
}
