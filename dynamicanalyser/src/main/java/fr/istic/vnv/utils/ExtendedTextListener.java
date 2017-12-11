package fr.istic.vnv.utils;

import fr.istic.vnv.App;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Human understandable text listener to write logs about tests being run.
 */
public class ExtendedTextListener extends RunListener {
    private int failuresCount;

    private static Logger log = LoggerFactory.getLogger(ExtendedTextListener.class);
    @Override
    public void testFailure(Failure failure) throws Exception {
        super.testFailure(failure);
        failuresCount++;

        log.warn("Test Failed for: {}", failure.toString());
        if(log.isDebugEnabled())
            failure.getException().printStackTrace(App.sysout);
    }

    public int getFailuresCount() {
        return failuresCount;
    }
}
