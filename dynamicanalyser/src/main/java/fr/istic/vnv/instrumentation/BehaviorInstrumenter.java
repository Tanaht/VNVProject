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

    /**
     * Manipulate Methods and Constructors to be aware when methods are being called and with what kind of parameters.
     * @throws CannotCompileException
     */
    private void traceExecutionInstrumentation() throws CannotCompileException {
        ctBehavior.insertBefore(new Callback("$args") {
            @Override
            public void result(Object[] objects) {
                Object[] args = (Object[]) objects[0];
                String trace = "[START]" + ctBehavior.getDeclaringClass().getName() + '.' + ctBehavior.getName();

                trace += "(";
                for (Object object : args) {
                    if(object != null) {
//                        TODO: When time comes generate a helper to pretty print method parameters to handle primitive type and other well known type (List, Map, String, int, double)
//                        For Other object we will only print his hashcode.
                        trace += object.toString().length() > 30 ? object.hashCode() : object.toString() + ", ";
                    } else
                        trace += "null, ";
                }
                trace += ")";

                AnalysisContext.getAnalysisContext().addExecutionTrace(trace);
            }
        }.sourceCode());

        ctBehavior.insertAfter(new Callback("\"\"") {
            @Override
            public void result(Object[] objects) {
                AnalysisContext.getAnalysisContext().addExecutionTrace("[END]");
            }
        }.sourceCode());
    }

    public void instrument() {
        if(!this.type.equals(ClassInstrumenter.CLASS.COMMON)) {
            log.trace("{} is not instrumented because not common class", this.ctBehavior.getDeclaringClass().getName());
            return;
        }

        if(this.ctBehavior.isEmpty()) {
            log.trace("{} is not instrumented because empty method body", this.ctBehavior.getLongName());
            return;
        }

        try {
            traceExecutionInstrumentation();
        } catch (CannotCompileException e) {
            log.warn("Unable to instrument {}, cause {}", this.ctBehavior.getLongName(), e.getReason());
        }
    }
}
