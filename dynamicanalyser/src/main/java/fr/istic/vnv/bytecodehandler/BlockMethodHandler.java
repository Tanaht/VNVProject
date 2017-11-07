package fr.istic.vnv.bytecodehandler;

import javassist.CtMethod;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.analysis.ControlFlow;
import javassist.bytecode.analysis.FramePrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockMethodHandler extends MethodHandler {

    private static final Logger log = LoggerFactory.getLogger(BlockMethodHandler.class);

    private FramePrinter printer;
    public BlockMethodHandler(CtMethod method) {
        super(method);
        this.printer = new FramePrinter(System.err);
    }

    @Override
    void handle() {
        log.info("START ================ {} ================", this.getCtMethod().getName());
        printer.print(this.getCtMethod());
        try {
            this.performFlowAnalysis();
        } catch (BadBytecode badBytecode) {
            log.error("BadBytecode Exception thrown: {}", badBytecode.getMessage());
        }
        log.info("END ================ {} ================", this.getCtMethod().getName());
    }

    private void performFlowAnalysis() throws BadBytecode {
        CodeAttribute codeAttribute = this.getCtMethod().getMethodInfo().getCodeAttribute();
        ControlFlow flow = new ControlFlow(this.getCtMethod());

        ControlFlow.Block blocks[] = flow.basicBlocks();
        LineNumberAttribute lineNumberAttribute = (LineNumberAttribute) codeAttribute.getAttribute(LineNumberAttribute.tag);

        for(ControlFlow.Block block : blocks) {
            if(block == null)
                continue;

            //log.debug("Block: {}", block.toString());
            //log.debug("Start Frame of instruction {} at line {}: {}", InstructionPrinter.instructionString(codeAttribute.iterator(), block.position(), codeAttribute.getConstPool()), lineNumberAttribute.toLineNumber(block.position()), flow.frameAt(block.position()));
            //log.debug("End Frame of instruction {} at line {}: {}", InstructionPrinter.instructionString(codeAttribute.iterator(), block.position() + block.length() - 1, codeAttribute.getConstPool()), lineNumberAttribute.toLineNumber(block.position() + block.length() - 1), flow.frameAt(block.position() + block.length() - 1));

        }

    }
}
