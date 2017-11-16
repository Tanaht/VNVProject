package fr.istic.vnv.instrumentation;

import fr.istic.vnv.Analysis.AnalysisContext;
import javassist.*;
import javassist.tools.Callback;

public class ClassInstrumenter {
    private CtClass ctClass;

    public ClassInstrumenter(CtClass ctClass) {
        this.ctClass = ctClass;
    }

    public void instrument() {
        for(CtBehavior ctBehavior : this.ctClass.getDeclaredBehaviors()) {
            Callback callback = new Callback("$args") {
                @Override
                public void result(Object[] objects) {
                    Object[] args = (Object[]) objects[0];
                    String trace = "[START]" + ctBehavior.getName();

                    trace += "(";
                    for (Object object : args) {
                        trace += object.toString() + ", ";
                    }
                    trace += ")";

                    AnalysisContext.getAnalysisContext().addExecutionTrace(trace);
                }
            };

            try {
                ctBehavior.insertBefore(callback.sourceCode());
            } catch (CannotCompileException e) {
                e.printStackTrace();
            }
        }

        for(CtBehavior ctBehavior : this.ctClass.getDeclaredBehaviors()) {
            Callback callback = new Callback("$args") {
                @Override
                public void result(Object[] objects) {
                    Object[] args = (Object[]) objects[0];
                    String trace = "[END]" + ctBehavior.getName();

                    trace += "(";
                    for (Object object : args) {
                        trace += object.toString() + ", ";
                    }
                    trace += ")";

                    AnalysisContext.getAnalysisContext().addExecutionTrace(trace);
                }
            };

            try {
                ctBehavior.insertAfter(callback.sourceCode());
            } catch (CannotCompileException e) {
                e.printStackTrace();
            }
        }
    }
}
