package fr.istic.vnv.instrumentation;

import fr.istic.vnv.App;
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

    @Override
    protected void branchCoverageInstrumentation() throws BadBytecode {


        CodeAttribute codeAttribute = this.getCtBehavior().getMethodInfo().getCodeAttribute();
        ControlFlow flow = new ControlFlow((CtMethod) this.getCtBehavior());

        ControlFlow.Block[] blocks = flow.basicBlocks();

        if(blocks.length == 1)
            return;// TODO: for now we focus on multiple blocks

//        InstructionPrinter.print((CtMethod) this.getCtBehavior(), System.err);
        log.debug("Number of blocks: {}", blocks.length);

        int index = 0;

        while(index < blocks.length) {
            flow = new ControlFlow((CtMethod) this.getCtBehavior());
            blocks = flow.basicBlocks();

            ControlFlow.Block block = blocks[index++];

            try {
                CodeIterator iterator = codeAttribute.iterator();
                Bytecode bytecode = new Bytecode(codeAttribute.getConstPool());
                CtClass current = App.pool.get(this.getClass().getName());

                bytecode.addLdc("Callback of " + this.getCtBehavior().getLongName() + " at bytecode position " + block.position());
                bytecode.addInvokestatic(current, "staticMethod", "(Ljava/lang/String;)V");

                iterator.insertAt(block.position(), bytecode.get());
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void staticMethod(String tst) {
        log.info("Satic method called " + tst);
    }
}
