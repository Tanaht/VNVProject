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
    private ClassInstrumenter.CLASS type;


    public BehaviorInstrumenter(CtBehavior behavior, ClassInstrumenter.CLASS type) {
        this.ctBehavior = behavior;
        this.type = type;
    }

    private Callback getTraceCallback(String when) {
        return new Callback("$args") {
            @Override
            public void result(Object[] objects) {
                Object[] args = (Object[]) objects[0];
                String trace = "[" +  when + "]" + ctBehavior.getDeclaringClass().getName() + '.' + ctBehavior.getName();

                trace += "(";
                for (Object object : args) {
                    if(object != null)
                        trace += object.toString() + ", ";
                    else
                        trace += "null, ";
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
        if(!this.type.equals(ClassInstrumenter.CLASS.COMMON)) {
            log.info("{} is not instrumented because not common class", this.ctBehavior.getDeclaringClass().getName());
            return;
        }

        if(this.ctBehavior.isEmpty()) {
            log.info("{} is not instrumented because empty method body", this.ctBehavior.getLongName());
            return;
        }

        try {
            traceExecutionInstrumentation();
        } catch (CannotCompileException e) {
            log.warn("Unable to instrument {}, cause {}", this.ctBehavior.getLongName(), e.getReason());
        }
    }
}
