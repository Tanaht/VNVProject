package fr.istic.vnv.instrumentation;

import javassist.CtBehavior;
import javassist.CtClass;

import java.util.ArrayList;
import java.util.List;

public class ClassInstrumenter implements Instrumenter {
    private CtClass ctClass;

    private List<BehaviorInstrumenter> behaviorInstrumenters;

    public ClassInstrumenter(CtClass ctClass) {
        this.ctClass = ctClass;

        this.behaviorInstrumenters = new ArrayList<>();
        for(CtBehavior ctBehavior : this.ctClass.getDeclaredBehaviors())
            this.behaviorInstrumenters.add(new BehaviorInstrumenter(ctBehavior));
    }

    public void instrument() {

        for(BehaviorInstrumenter behaviorInstrumenter : behaviorInstrumenters)
            behaviorInstrumenter.instrument();
    }
}
