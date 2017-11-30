package fr.istic.vnv.instrumentation;

import javassist.CtConstructor;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.analysis.ControlFlow;

public class ConstructorInstrumenter extends BehaviorInstrumenter {

    public ConstructorInstrumenter(CtConstructor ctConstructor, ClassInstrumenter.CLASS type) {
        super(ctConstructor, type);
    }
}
