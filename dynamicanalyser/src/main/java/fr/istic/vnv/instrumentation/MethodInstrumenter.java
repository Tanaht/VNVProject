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

    @Override
    protected void branchCoverageInstrumentation() throws BadBytecode {

        CodeAttribute codeAttribute = this.getCtBehavior().getMethodInfo().getCodeAttribute();
        ControlFlow flow = new ControlFlow((CtMethod) this.getCtBehavior());

        ControlFlow.Block[] blocks = flow.basicBlocks();
        LineNumberAttribute lineNumberAttribute = (LineNumberAttribute) codeAttribute.getAttribute(LineNumberAttribute.tag);

        if(blocks.length == 1)
            return;// TODO: for now we focus on multiple blocks

        int index = 0;

        while(index < blocks.length) {

            flow = new ControlFlow((CtMethod) this.getCtBehavior());
            blocks = flow.basicBlocks();
            lineNumberAttribute = (LineNumberAttribute) codeAttribute.getAttribute(LineNumberAttribute.tag);
            ControlFlow.Block block = blocks[index++];

            try {
                CodeIterator iterator = codeAttribute.iterator();
                Bytecode bytecode = new Bytecode(codeAttribute.getConstPool());
                CtClass current = App.pool.get(this.getClass().getName());

                // TODO: For Now We record branch coverage on each start of block, it should be done on each line of each block
                bytecode.addLdc(this.getCtBehavior().getDeclaringClass().getName());
                // Here we use getDescriptor to avoid anything about polymorphism in input project.
                bytecode.addLdc(this.getCtBehavior().getName() + this.getCtBehavior().getMethodInfo().getDescriptor());
                bytecode.addIconst(block.index());
                bytecode.addIconst(lineNumberAttribute.toLineNumber(block.position()));

                AnalysisContext.createBranchCoverage(this.getCtBehavior().getDeclaringClass().getName(),
                        this.getCtBehavior().getName() + this.getCtBehavior().getMethodInfo().getDescriptor(),
                        block.index(),
                        lineNumberAttribute.toLineNumber(block.position())
                );

                bytecode.addInvokestatic(current, "reportBranchCoverage", "(Ljava/lang/String;Ljava/lang/String;II)V");

                iterator.insertAt(block.position(), bytecode.get());
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This Method is executed by the input project to record branch coverage datas.
     * @param classname
     * @param methodName
     * @param blockIndex
     * @param lineNumber
     */
    public static void reportBranchCoverage(String classname, String methodName, int blockIndex, int lineNumber) {
        AnalysisContext.reportBranchCoverage(classname, methodName, blockIndex, lineNumber);
    }
}
