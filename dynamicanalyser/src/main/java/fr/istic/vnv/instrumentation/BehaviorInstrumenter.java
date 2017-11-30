package fr.istic.vnv.instrumentation;

import fr.istic.vnv.App;
import fr.istic.vnv.analysis.AnalysisContext;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.analysis.ControlFlow;
import javassist.tools.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A CtBehavior object is either a CtMethod or CtConstructor object.
 */
public abstract class BehaviorInstrumenter implements Instrumenter {

    private static Logger log = LoggerFactory.getLogger(BehaviorInstrumenter.class);
    private CtBehavior ctBehavior;
    private ClassInstrumenter.CLASS type;
    private CtClass self;

    public BehaviorInstrumenter(CtBehavior behavior, ClassInstrumenter.CLASS type) {
        this.ctBehavior = behavior;
        this.type = type;

        try {
            this.self = App.pool.get(this.getClass().getName());
        }
        catch (NotFoundException e) {
            log.error(e.getMessage());
        }
    }

    public CtBehavior getCtBehavior() {
        return ctBehavior;
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

        log.debug("Instrument class {}", ctBehavior.getLongName());

        try {
            log.trace("Branch Coverage Instrumentation of {}", ctBehavior.getLongName());
            branchCoverageInstrumentation();
        } catch (BadBytecode badBytecode) {
            log.error("Unable to perform branch coverage instrumentation {}, cause {}", this.ctBehavior.getLongName(), badBytecode.getMessage());

            if(log.isTraceEnabled())
                badBytecode.printStackTrace();
        }

        try {
            log.trace("Execution Trace Instrumentation of {}", ctBehavior.getLongName());
            traceExecutionInstrumentation();
        } catch (CannotCompileException e) {
            log.error("Unable to perform trace execution instrumentation {}, cause {}", this.ctBehavior.getLongName(), e.getReason());
        }
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

    protected void branchCoverageInstrumentation() throws BadBytecode {

        CodeAttribute codeAttribute = this.getCtBehavior().getMethodInfo().getCodeAttribute();
        ControlFlow flow = new ControlFlow(this.getCtBehavior().getDeclaringClass(), this.getCtBehavior().getMethodInfo());

        ControlFlow.Block[] blocks = flow.basicBlocks();
        LineNumberAttribute lineNumberAttribute = (LineNumberAttribute) codeAttribute.getAttribute(LineNumberAttribute.tag);

        if(blocks.length == 1)
            return;// TODO: for now we focus on multiple blocks

        int index = 0;

        while(index < blocks.length) {

            flow = new ControlFlow(this.getCtBehavior().getDeclaringClass(), this.getCtBehavior().getMethodInfo());
            blocks = flow.basicBlocks();
            lineNumberAttribute = (LineNumberAttribute) codeAttribute.getAttribute(LineNumberAttribute.tag);
            ControlFlow.Block block = blocks[index++];

                CodeIterator iterator = codeAttribute.iterator();
                Bytecode bytecode = new Bytecode(codeAttribute.getConstPool());

                // TODO: For Now We record branch coverage on each start of block, it should be done on each line of each block
                insertInvokeStatic(bytecode, block.index(), lineNumberAttribute.toLineNumber(block.position()));

                iterator.insertAt(block.position(), bytecode.get());
        }
    }

    private void insertInvokeStatic(Bytecode bytecode, int blockIndex, int lineNumber) {
        bytecode.addLdc(this.getCtBehavior().getDeclaringClass().getName());
        // Here we use getDescriptor to avoid anything about polymorphism in input project.
        bytecode.addLdc(this.getCtBehavior().getName() + this.getCtBehavior().getMethodInfo().getDescriptor());
        bytecode.addIconst(blockIndex);
        bytecode.addIconst(lineNumber);

        AnalysisContext.createBranchCoverage(this.getCtBehavior().getDeclaringClass().getName(),
                this.getCtBehavior().getName() + this.getCtBehavior().getMethodInfo().getDescriptor(),
                blockIndex,
                lineNumber
        );

        bytecode.addInvokestatic(this.self, "reportBranchCoverage", "(Ljava/lang/String;Ljava/lang/String;II)V");
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
