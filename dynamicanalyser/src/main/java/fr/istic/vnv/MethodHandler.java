package fr.istic.vnv;

import javassist.CtMethod;
import javassist.bytecode.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodHandler {

    private static Logger log = LoggerFactory.getLogger(MethodHandler.class);

    private CtMethod ctMethod;

    public MethodHandler(CtMethod method) {
        this.ctMethod = method;
    }

    void handle() {
        log.info("Handling {}", this.ctMethod.getName());
        CodeAttribute codeAttribute = this.ctMethod.getMethodInfo().getCodeAttribute();

        LineNumberAttribute lineNumberAttribute = (LineNumberAttribute) codeAttribute.getAttribute(LineNumberAttribute.tag);

        for(int i = 0 ; i < lineNumberAttribute.tableLength() ; i++) {
            //log.debug("[{}] {}", lineNumberAttribute.lineNumber(i), InstructionPrinter.instructionString(codeAttribute.iterator(), lineNumberAttribute.startPc(i), lineNumberAttribute.getConstPool()));

            try {
                insertToStringFragments(codeAttribute.iterator(), lineNumberAttribute.toStartPc(i), "[" + ctMethod.getLongName() + " Line " + lineNumberAttribute.lineNumber(i) + "]");
                codeAttribute.computeMaxStack();
            } catch (BadBytecode badBytecode) {
               log.error("Failed to rewrite bytecode correctly: {}", badBytecode.getMessage());
            }
        }
    }

    private void insertToStringFragments(CodeIterator codeIterator, int index, String message) throws BadBytecode {
        if(index < 0) {
            //log.warn(": {}", ctMethod.getLongName());
            index = 0;
        }
        Bytecode bytecode = new Bytecode(this.ctMethod.getMethodInfo().getConstPool());

        bytecode.addGap(1);
        bytecode.addPrintln(message);
        bytecode.addGap(1);

        codeIterator.insertAt(index, bytecode.get());
    }
}
