package fr.istic.vnv;

import javassist.CtClass;
import javassist.CtMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ClassHandler {

    private static Logger log = LoggerFactory.getLogger(ClassHandler.class);

    private CtClass ctClass;
    private List<MethodHandler> methods;

    public ClassHandler(CtClass ctClass) {
        this.ctClass = ctClass;
        this.methods = new ArrayList<>();

        for(CtMethod method : this.ctClass.getDeclaredMethods()) {
            this.methods.add(new MethodHandler(method));
        }
    }

    void handle() {
        log.info("Handling {}", this.ctClass.getName());

        for(MethodHandler handler : this.methods) {
            handler.handle();
        }
    }
}
