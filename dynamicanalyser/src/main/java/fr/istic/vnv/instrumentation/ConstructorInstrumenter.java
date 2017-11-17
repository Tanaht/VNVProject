package fr.istic.vnv.instrumentation;

import javassist.CtConstructor;
import javassist.bytecode.BadBytecode;

public class ConstructorInstrumenter extends BehaviorInstrumenter {

    public ConstructorInstrumenter(CtConstructor ctConstructor, ClassInstrumenter.CLASS type) {
        super(ctConstructor, type);
    }

    @Override
    protected void branchCoverageInstrumentation() throws BadBytecode {
        //Do nothing
    }
}
