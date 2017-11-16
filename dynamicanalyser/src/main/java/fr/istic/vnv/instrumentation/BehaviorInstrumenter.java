package fr.istic.vnv.instrumentation;

import fr.istic.vnv.Analysis.AnalysisContext;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.tools.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A CtBehavior object is either a CtMethod or CtConstructor object.
 */
public class BehaviorInstrumenter implements Instrumenter {

    private Logger log = LoggerFactory.getLogger(BehaviorInstrumenter.class);
    private CtBehavior ctBehavior;

    public BehaviorInstrumenter(CtBehavior behavior) {
        this.ctBehavior = behavior;
    }

    private Callback getTraceCallback(String when) {
        return new Callback("$args") {
            @Override
            public void result(Object[] objects) {
                Object[] args = (Object[]) objects[0];
                String trace = "[" +  when + "]" + ctBehavior.getName();

                trace += "(";
                for (Object object : args) {
                    trace += object.toString() + ", ";
                }
                trace += ")";

                AnalysisContext.getAnalysisContext().addExecutionTrace(trace);
            }
        };
    }

    private void traceExecutionInstrumentation() throws CannotCompileException {
        ctBehavior.insertBefore(getTraceCallback("START").sourceCode());
        ctBehavior.insertAfter(getTraceCallback("END").sourceCode());
    }

    public void instrument() {
        try {
            traceExecutionInstrumentation();
        } catch (CannotCompileException e) {
            log.info("Unable to instrument {], cause {}", this.ctBehavior.getLongName(), e.getReason());
        }
    }
}
