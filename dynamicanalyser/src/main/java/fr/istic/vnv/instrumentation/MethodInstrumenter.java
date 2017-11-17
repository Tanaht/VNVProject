package fr.istic.vnv.instrumentation;

import javassist.CtMethod;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.analysis.ControlFlow;

public class MethodInstrumenter extends BehaviorInstrumenter {

    public MethodInstrumenter(CtMethod method, ClassInstrumenter.CLASS type) {
        super(method, type);
    }

    @Override
    protected void branchCoverageInstrumentation() throws BadBytecode {
        CodeAttribute codeAttribute = this.getCtBehavior().getMethodInfo().getCodeAttribute();
        ControlFlow flow = new ControlFlow((CtMethod) this.getCtBehavior());

        ControlFlow.Block[] blocks = flow.basicBlocks();
    }
}
