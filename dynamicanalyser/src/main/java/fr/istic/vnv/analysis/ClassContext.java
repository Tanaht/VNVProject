package fr.istic.vnv.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Class representation to see what is it executed in a class.
 */
public class ClassContext {
    private String longName;
    private Map<String, BehaviorContext> behaviorContexts;

    /**
     *
     * @param longName The name of the class associate with it package. Example namePackage.ClassName
     */
    public ClassContext(String longName) {
        this.longName = longName;
        this.behaviorContexts = new HashMap<>();
    }

    /**
     * Retrieve ClassContext, if not found, create it.
     * @param methodName The name of the method and it descriptor you want to get it BehaviorContext.
     * @return The BehaviorContext who represent the method execution.
     */
    public BehaviorContext getBehaviorContext(String methodName) {
        Optional<BehaviorContext> behaviorContextOptional = Optional.ofNullable(this.behaviorContexts.get(methodName));

        if(behaviorContextOptional.isPresent())
            return behaviorContextOptional.get();

        BehaviorContext behaviorContext = new BehaviorContext(methodName);
        this.behaviorContexts.put(methodName, behaviorContext);
        return behaviorContext;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("###");
        builder.append(this.longName);
        builder.append(":\n");

        for(String methodName : this.behaviorContexts.keySet()) {
            builder.append(this.behaviorContexts.get(methodName));
        }

        return builder.toString();
    }
}
