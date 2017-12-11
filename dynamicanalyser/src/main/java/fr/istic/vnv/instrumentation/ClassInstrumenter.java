package fr.istic.vnv.instrumentation;

import javassist.CtBehavior;
import javassist.CtClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ClassInstrumenter implements Instrumenter {
    private static Logger log = LoggerFactory.getLogger(ClassInstrumenter.class);

    private CtClass ctClass;

    private List<BehaviorInstrumenter> behaviorInstrumenters;

    public enum CLASS {
        INTERFACE,
        ENUM,
        ARRAY,
        ANNOTATION,
        ABSTRACT,
        COMMON;
    }

    public ClassInstrumenter(CtClass ctClass) {
        this.ctClass = ctClass;
        CLASS type = getClassType();

        this.behaviorInstrumenters = new ArrayList<>();

        for(CtBehavior ctBehavior: this.ctClass.getDeclaredBehaviors())
            this.behaviorInstrumenters.add(new BehaviorInstrumenter(ctBehavior, type));
    }

    public void instrument() {
        log.debug("Instrument class {}", ctClass.getName());
        for(BehaviorInstrumenter behaviorInstrumenter : behaviorInstrumenters)
            behaviorInstrumenter.instrument();
    }

    /**
     * return appropriate ClassInstrumenter.CLASS value according to class type (Interface, Enum, Common Class).
     * @return ClassInstrumenter.CLASS
     */
    private CLASS getClassType() {
        if(this.ctClass.isInterface())
            return CLASS.INTERFACE;

        if(this.ctClass.isEnum())
            return CLASS.ENUM;

        if(this.ctClass.isArray())
            return CLASS.ARRAY;

        if(this.ctClass.isAnnotation())
            return CLASS.ANNOTATION;

        if(this.ctClass.getClassFile().isAbstract())
            return CLASS.ABSTRACT;

        return CLASS.COMMON;
    }
}
