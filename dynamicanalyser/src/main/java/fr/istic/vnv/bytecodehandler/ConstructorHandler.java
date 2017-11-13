package fr.istic.vnv.bytecodehandler;

import javassist.CtConstructor;

public abstract class ConstructorHandler {
    private CtConstructor ctConstructor;

    public ConstructorHandler(CtConstructor constructor) {
        this.ctConstructor = constructor;
    }

    public CtConstructor getCtConstructor() {
        return ctConstructor;
    }

    abstract void handle();
}
