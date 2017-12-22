package fr.istic.vnv.instrumentation;

import fr.istic.vnv.analysis.AnalysisContext;
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

    /**
     * initialize a class instrumentation
     * @param ctClass class to instrument
     */
    public ClassInstrumenter(CtClass ctClass) {
        this.ctClass = ctClass;
        CLASS type = getClassType();

        this.behaviorInstrumenters = new ArrayList<>();

        for(CtBehavior ctBehavior: this.ctClass.getDeclaredBehaviors())
            this.behaviorInstrumenters.add(new BehaviorInstrumenter(ctBehavior, type));

        this.ctClass.getClassPool().importPackage("fr.istic.vnv.analysis");
    }

    /**
     * Start the instrument of the class gived in the constructor
     */
    public void instrument() {
        log.debug("Instrument class {}", ctClass.getName());
        for(BehaviorInstrumenter behaviorInstrumenter : behaviorInstrumenters) {
            if(!AnalysisContext.getAnalysisContext().isInstrumented(behaviorInstrumenter.getCtBehavior().getLongName() + behaviorInstrumenter.getCtBehavior().getSignature())) {
                AnalysisContext.getAnalysisContext().instrument(behaviorInstrumenter.getCtBehavior().getLongName() + behaviorInstrumenter.getCtBehavior().getSignature());
                behaviorInstrumenter.instrument();
            }
        }
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
