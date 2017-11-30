package fr.istic.vnv.instrumentation;

import fr.istic.vnv.App;
import fr.istic.vnv.analysis.AnalysisContext;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Loader;
import javassist.NotFoundException;
import javassist.bytecode.*;
import javassist.bytecode.analysis.ControlFlow;
import javassist.tools.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodInstrumenter extends BehaviorInstrumenter {

    private static Logger log = LoggerFactory.getLogger(MethodInstrumenter.class);

    public MethodInstrumenter(CtMethod method, ClassInstrumenter.CLASS type) {
        super(method, type);
    }
}
