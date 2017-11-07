package fr.istic.vnv.bytecodehandler;

import javassist.CtClass;
import javassist.CtMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class ClassHandler {

    private static Logger log = LoggerFactory.getLogger(ClassHandler.class);

    private CtClass ctClass;
    private List<MethodHandler> methods;

    public ClassHandler(CtClass ctClass) {
        this.ctClass = ctClass;
        this.methods = new ArrayList<>();

        for(CtMethod method : this.ctClass.getDeclaredMethods()) {
            this.methods.add(this.createMethodHandlerFor(method));
        }
    }

    public CtClass getCtClass() {
        return ctClass;
    }

    public List<MethodHandler> getMethodHandlers() {
        return methods;
    }

    public abstract MethodHandler createMethodHandlerFor(CtMethod method);

    public abstract void handle();
}
