package fr.istic.vnv.bytecodehandler;

import javassist.CannotCompileException;
import javassist.CtConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleConstructorHandler extends ConstructorHandler {

    private static final Logger log = LoggerFactory.getLogger(SimpleConstructorHandler.class);

    public SimpleConstructorHandler(CtConstructor constructor) {
        super(constructor);
    }

    @Override
    void handle() {
        log.info("START ================ {} ================", this.getCtConstructor().getName());

        try {
            this.getCtConstructor().insertBefore("{ String message = \"" + this.getCtConstructor().getLongName() + "(\";\n" +
                    "        for(int i = 0 ; i < $args.length ; i++) {\n" +
                    "            message += $args[i] + \", \";\n" +
                    "        }\n" +
                    "        System.err.println(message + \");\"); }");
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
        log.info("END ================ {} ================", this.getCtConstructor().getName());    }
}
