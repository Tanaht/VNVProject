package fr.istic.vnv.metadata;

import javassist.*;

public class MetadataClassGenerator {
    private ClassPool pool;

    public MetadataClassGenerator(ClassPool pool) { this.pool = pool; }

    public CtClass generateSingleton() throws CannotCompileException {
        CtClass coverage = this.pool.makeClass("CoverageMetadata");
        pool.importPackage("java.util.List");


        CtNewConstructor.make("private CoverageMetadata() {\n" +
                "        this.executionTrace = new ArrayList();\n" +
                "    }", coverage);
        CtField.make("private static CoverageMetadata instance;", coverage);
        CtField.make("private List executionTrace;", coverage);


        CtNewMethod.make("private static CoverageMetadata get() {\n" +
                "        if(instance == null)\n" +
                "            instance = new CoverageMetadata();\n" +
                "\n" +
                "        return instance;\n" +
                "    }", coverage);

        CtNewMethod.make("public void execute(String className, int lineNumber, String methodName) {\n" +
                "        get().executionTrace.add(\"[\" + lineNumber + \"]\" + className + \" \" + methodName);\n" +
                "    }", coverage);

        return coverage;
    }
}
