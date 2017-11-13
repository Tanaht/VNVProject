package fr.istic.vnv.bytecodehandler;

import javassist.CtClass;
import javassist.CtConstructor;
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
    public ConstructorHandler createConstructorHandlerFor(CtConstructor ctConstructor) {
        return new SimpleConstructorHandler(ctConstructor);
    }

    @Override
    public void handle() {
        for(ConstructorHandler handler : this.getConstructorHandlers())
            handler.handle();

        for(MethodHandler handler : this.getMethodHandlers())
            handler.handle();
    }
}
