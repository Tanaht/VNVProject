package fr.istic.vnv.instrumentation;

import fr.istic.vnv.App;
import fr.istic.vnv.analysis.AnalysisContext;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.analysis.ControlFlow;
import javassist.tools.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
            log.trace("Line Coverage Instrumentation of {}", ctBehavior.getLongName());
            lineCoverageInstrumentation();
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

    protected void lineCoverageInstrumentation() throws BadBytecode {

        CodeAttribute codeAttribute = this.getCtBehavior().getMethodInfo().getCodeAttribute();
        ControlFlow flow = new ControlFlow(this.getCtBehavior().getDeclaringClass(), this.getCtBehavior().getMethodInfo());
        ControlFlow.Block[] blocks = flow.basicBlocks();
        Bytecode bytecode = null;

        int index = 0;

        while(index < blocks.length) {
            //Redefine flow, blocks and iterator because when manipulating bytecode in a loop it will fail.
            flow = new ControlFlow(this.getCtBehavior().getDeclaringClass(), this.getCtBehavior().getMethodInfo());
            blocks = flow.basicBlocks();
            ControlFlow.Block block = blocks[index++];

            CodeIterator iterator = codeAttribute.iterator();

            //Retrieve list of lines inside this block and matching bytecode index.
            List<LineNumberAttribute.Pc> pcs = getLineNumbersBetween(codeAttribute, block.position(), block.position() + block.length());

            for(int i = pcs.size() -1 ; i >= 0 ; i--) {
//                log.trace("{} block {}, pc line {}, pc index {}", this.getCtBehavior().getLongName(), block.index(), pcs.get(i).line, pcs.get(i).index);
                bytecode = new Bytecode(codeAttribute.getConstPool());
                insertLineCoverageCallback(bytecode, block.index(), pcs.get(i).line);
                iterator.insertAt(pcs.get(i).index, bytecode.get());
            }

        }
    }

    private void insertLineCoverageCallback(Bytecode bytecode, int blockIndex, int lineNumber) {
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


        //override first item in resulting array because the startPc doesn't match in all case the start of a source code line.
        LineNumberAttribute.Pc pc = new LineNumberAttribute.Pc();
        pc.index = startPc; pc.line = startLine;
        opcodesIndexes.set(0, pc);

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
