package fr.istic.vnv.instrumentation;

import fr.istic.vnv.App;
import fr.istic.vnv.analysis.AnalysisContext;
import fr.istic.vnv.utils.Config;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.*;
import javassist.bytecode.analysis.ControlFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A CtBehavior object is either a CtMethod or CtConstructor object.
 */
public class BehaviorInstrumenter implements Instrumenter {

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

    /**
     * Return Basic Block list of CtBehavior
     * @return
     * @throws BadBytecode
     */
    private ControlFlow.Block[] getBlocks() throws BadBytecode {
        return new ControlFlow(this.ctBehavior.getDeclaringClass(), this.ctBehavior.getMethodInfo()).basicBlocks();
    }

    public void instrument() {
        if (!this.type.equals(ClassInstrumenter.CLASS.COMMON)) {
            log.trace("{} is not instrumented because not common class", this.ctBehavior.getDeclaringClass().getName());
            return;
        }

        if (this.ctBehavior.isEmpty()) {
            log.trace("{} is not instrumented because empty method body", this.ctBehavior.getLongName());
            return;
        }

        log.trace("Instrument method {}{}", ctBehavior.getName(), Descriptor.toString(ctBehavior.getSignature()));

        if(Config.get().doBranchCoverage()) {
            try {
                log.trace("Line Coverage Instrumentation of {}", ctBehavior.getLongName());
                lineCoverageInstrumentation();
            } catch (BadBytecode badBytecode) {
                log.error("Unable to perform branch coverage instrumentation {}, cause {}", this.ctBehavior.getLongName(), badBytecode.getMessage());
                if (log.isDebugEnabled())
                    badBytecode.printStackTrace(App.syserr);
            }
        }

        if(Config.get().doExecutionTrace()) {
            try {
                log.trace("Execution Trace Instrumentation of {}", ctBehavior.getLongName());
                traceExecutionInstrumentation();
            } catch (CannotCompileException e) {
                log.error("Unable to perform trace execution instrumentation {}, cause {}", this.ctBehavior.getLongName(), e.getReason());

                if (log.isDebugEnabled())
                    e.printStackTrace(App.syserr);
            }
        }
    }

    /**
     * Manipulate Methods and Constructors to be aware when methods are being called and with what kind of parameters.
     * @throws CannotCompileException
     */
    private void traceExecutionInstrumentation() throws CannotCompileException {
        String beforeInstr = ctBehavior.getDeclaringClass().getName() + '.' + ctBehavior.getName();
        ctBehavior.insertBefore("{ AnalysisContext.addStartExecutionTrace(\"" + beforeInstr + "\", $args); }");
    }

    protected void lineCoverageInstrumentation() throws BadBytecode {
        CodeAttribute codeAttribute = this.ctBehavior.getMethodInfo().getCodeAttribute();

        int current = 0;
        while(current < getBlocks().length) {

            instrumentBlockIndexedAt(codeAttribute, getBlocks()[current++].index());
        }
    }

    private void instrumentBlockIndexedAt(CodeAttribute codeAttribute, int blockIndex) throws BadBytecode {
        ControlFlow.Block[] blocks = getBlocks();
        Bytecode bytecode = null;

        if(blocks.length < blockIndex) {
            throw new ArrayIndexOutOfBoundsException("Blocks List doesn't have index " + blockIndex);
        }

        ControlFlow.Block block = blocks[blockIndex];

        if(block.index() != blockIndex) {
            throw new ArrayIndexOutOfBoundsException("retrieved block instance " + block.index() + " doesn't have correct index " + blockIndex);
        }

        CodeIterator iterator = codeAttribute.iterator();
        List<LineNumberAttribute.Pc> pcs = getLineNumbersBetween(codeAttribute, block.position(), block.position() + block.length());

        for(int i = pcs.size() -1 ; i >= 0 ; i--) {
            log.trace("{} block {}, pc line {}, pc index {}", this.ctBehavior.getLongName(), block.index(), pcs.get(i).line, pcs.get(i).index);
            bytecode = new Bytecode(codeAttribute.getConstPool());
            insertLineCoverageCallback(bytecode, block.index(), pcs.get(i).line);
            iterator.insertAt(pcs.get(i).index, bytecode.get());

            // there is a way to optimize this ?
            codeAttribute.computeMaxStack();
            block = getBlocks()[blockIndex];
            pcs = getLineNumbersBetween(codeAttribute, block.position(), block.position() + block.length());
        }
    }

    private void insertLineCoverageCallback(Bytecode bytecode, int blockIndex, int lineNumber) {
        if(AnalysisContext.getAnalysisContext().isInstrumented(this.ctBehavior.getName() + this.ctBehavior.getSignature()))
            log.error("It is being reinstrumented");
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

        bytecode.addInvokestatic(this.self, "reportLineCoverage", "(Ljava/lang/String;Ljava/lang/String;II)V");
    }

    /**
     * Return a List of Pc instances (a bytecode index and a line number) that is between startPc end end Pc
     * @param codeAttribute
     * @param startPc include
     * @param endPc exclude
     * @return
     */
    private List<LineNumberAttribute.Pc> getLineNumbersBetween(CodeAttribute codeAttribute, int startPc, int endPc) {
        //Reload LineNumberAttribute, because CodeAttribute change at each computeMaxStack() method calls
        LineNumberAttribute lineNumberAttribute = (LineNumberAttribute) codeAttribute.getAttribute(LineNumberAttribute.tag);

        List<LineNumberAttribute.Pc> opcodesIndexes = new ArrayList<>();
        List<Integer> coveredLines = new ArrayList<>();

        int startLine = lineNumberAttribute.toLineNumber(startPc);
        int endLine = lineNumberAttribute.toLineNumber(endPc - 1);

        //Compute list of lines that is covered by array of bytecode between index in parameter
        for(int i = 0 ; i < lineNumberAttribute.tableLength() ; i++) {

            if(lineNumberAttribute.lineNumber(i) > endLine)
                break;

            if(lineNumberAttribute.lineNumber(i) >= startLine) {
                coveredLines.add(lineNumberAttribute.lineNumber(i));
            }
        }

        //Retrieve appropriate bytecode index of each source code lines computed precedently
        for(int i = 0 ; i < coveredLines.size(); i++) {
            opcodesIndexes.add(lineNumberAttribute.toNearPc(coveredLines.get(i)));
        }

        if(opcodesIndexes.size() > 0) {
            //override first item in resulting array because the startPc doesn't match in all case the start of a source code line.
            LineNumberAttribute.Pc pc = new LineNumberAttribute.Pc();
            pc.index = startPc; pc.line = startLine;
            opcodesIndexes.set(0, pc);
        }

        return opcodesIndexes;
    }

    /**
     * This Method is executed by the input project to record branch coverage datas.
     * @param classname
     * @param methodName
     * @param blockIndex
     * @param lineNumber
     */
    public static void reportLineCoverage(String classname, String methodName, int blockIndex, int lineNumber) {
        AnalysisContext.reportBranchCoverage(classname, methodName, blockIndex, lineNumber);
    }
}
