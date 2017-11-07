package fr.istic.vnv.bytecodehandler;

import javassist.CtClass;
import javassist.CtMethod;

public class BlockClassHandler extends ClassHandler {

    public BlockClassHandler(CtClass ctClass) {
        super(ctClass);
    }

    @Override
    public MethodHandler createMethodHandlerFor(CtMethod method) {
        return new BlockMethodHandler(method);
    }

    @Override
    public void handle() {
        for(MethodHandler handler : this.getMethodHandlers())
            handler.handle();
    }
}
