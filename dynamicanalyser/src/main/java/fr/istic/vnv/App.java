package fr.istic.vnv;

import fr.istic.vnv.Report.ReportGenerator;
import fr.istic.vnv.Report.ReportGeneratorFactory;
import fr.istic.vnv.instrumentation.ClassInstrumenter;
import javassist.*;
import javassist.bytecode.ClassFile;
import org.apache.commons.io.FileUtils;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 *
 */
public class App {
    private static Logger log = LoggerFactory.getLogger(App.class);

    public static ClassPool pool;

    public static void main(String[] args) {

        try {
            System.setOut(new PrintStream(new File("out.txt")));
            System.setErr(new PrintStream(new File("err.txt")));
        } catch (FileNotFoundException e) {
            log.error("Impossible to redirect standards output into out.txt and err.txt");
            if(log.isDebugEnabled())
                e.printStackTrace();
            return;
        }

        if (args.length != 1) {
            log.error("Please provide path to a maven project in argument to this program.");
            return;
        }

        File mavenProject = new File(args[0]);

        if (!mavenProject.exists() || !mavenProject.isDirectory() || !FileUtils.getFile(mavenProject, "pom.xml").exists()) {
            log.error("Please provide a valid path to a maven project in argument to this program.");
            return;
        }

        log.info("Start Dynamic analysis of {}", mavenProject.getAbsolutePath());

        File classesFolder = FileUtils.getFile(mavenProject, "target/classes");
        File testClassesFolder = FileUtils.getFile(mavenProject, "target/test-classes");

        if (!testClassesFolder.exists() || !classesFolder.exists()) {
            log.warn("There is no compiled test to run or no compiles source code to test.");
            return;
        }

        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[]{testClassesFolder.toURI().toURL(), classesFolder.toURI().toURL()});
            Collection<File> testSuites = FileUtils.listFiles(testClassesFolder, new String[]{"class"}, true);

            Stream<File> fileStream = testSuites.stream();

            testSuites = fileStream.filter(file -> !file.getName().contains("$")).filter(file -> file.getName().endsWith("Test.class")).collect(Collectors.toCollection(ArrayList::new));

            log.debug("Found {} test suites to run", testSuites.size());
            pool = ClassPool.getDefault();
            pool.importPackage("java.io");

            try {
                pool.appendClassPath(classesFolder.getPath());
                pool.appendClassPath(testClassesFolder.getPath());
            } catch (NotFoundException e) {
                if(log.isDebugEnabled())
                    e.printStackTrace();
            }

            Loader loader = new Loader(classLoader, pool);

            /*
             * Do not instrument the folowing packages:
             * (by default Javassist do not instrument java.lang.* and other native methods)
             */
            loader.delegateLoadingOf("org.junit.");
            loader.delegateLoadingOf("javassist.");
            loader.delegateLoadingOf("fr.istic.vnv.");

            try {
                loader.addTranslator(pool, new Translator() {
                    @Override
                    public void start(ClassPool pool) throws NotFoundException, CannotCompileException {}

                    @Override
                    public void onLoad(ClassPool pool, String classname) throws NotFoundException, CannotCompileException {
                        String classLocationPath = pool.find(classname).getPath().replace("\\", "/");

                        try{
                            String testFolderPath = testClassesFolder.getCanonicalPath().replace("\\", "/");
                            if(classLocationPath.contains(testFolderPath)) {
                                // TODO: Here it is a test Class that is being loaded,
                                // so perform appropriate bytecode manipulation if needed
                                // (like if we want to know what unit test has called what related project method, when we compute execution trace).
                                return;
                            }

                            log.trace("[JAVASSIST] {} {}", classLocationPath, classname);

                            ClassInstrumenter instrumenter = new ClassInstrumenter(pool.getCtClass(classname));

                            try{
                                instrumenter.instrument();
                            } catch (Exception e) {
                                log.error("Unable to instrument {}, cause {}", classname, e.getMessage());

                                if(log.isDebugEnabled())
                                    e.printStackTrace();
                            }

                        } catch (IOException e) {
                            log.error("Unable to define if {} is in {} or not", classLocationPath, testClassesFolder.getPath());

                            if(log.isDebugEnabled())
                                e.printStackTrace();
                        }
                    }
                });
            } catch (NotFoundException | CannotCompileException e) {
                log.error("Unable to instrument some method due to: {}", e.getMessage());

                if(log.isDebugEnabled())
                    e.printStackTrace();
            }


            int succeed = 0;
            for (File testSuite : testSuites) {
                ClassFile classFile = new ClassFile(new DataInputStream(new FileInputStream(testSuite.getAbsolutePath())));

                if(classFile.isAbstract()) {
                    log.trace("Abstract Test Class found: {}, it will be ignored", classFile.getName());
                    continue;
                }

                log.trace("Run TestSuite: {}", classFile.getName());
                Result result = JUnitCore.runClasses(loader.loadClass(classFile.getName()));

                if (!result.wasSuccessful()) {
                    log.error("{} Test Failed out of {} on {}", result.getFailureCount(), result.getRunCount(), classFile.getName());

                    for(Failure failure : result.getFailures()) {
                        log.warn("Test Failed for: {}", failure.toString());
                    }
                } else {
                    succeed++;
                    log.trace("All test in {}, was executed with success.", testSuite.getName());
                }
            }

            log.info("{} TestSuites out of {} was passed successfully", succeed, testSuites.size());

            ReportGenerator reportGenerator = ReportGeneratorFactory.getTextReportGenerator();

            PrintStream stream = new PrintStream(new File("VNVReport.txt"));
            reportGenerator.save(stream);
        } catch (ClassNotFoundException | IOException e) {
            log.warn("Exception {}", e.getMessage());
            log.error("An exception occured during analyses, please check git issues and create one if there is none of that kind at http://www.github.com/tanaht/VNVProject");

            if(log.isDebugEnabled())
                e.printStackTrace();
        }

    }

    public static String helloWorld() {
        return "Hello World";
    }


    public static String helloWhat(boolean b) {
        String returnMsg = "";
        if (b) {
            returnMsg = "True";
        } else {
            returnMsg = "False";
        }

        return "Hello " + returnMsg;
    }
}
