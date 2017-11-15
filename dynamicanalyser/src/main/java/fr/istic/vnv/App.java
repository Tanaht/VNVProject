package fr.istic.vnv;

import javassist.*;
import javassist.bytecode.ClassFile;
import org.apache.commons.io.FileUtils;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 *
 */
public class App {
    private static Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            log.error("Please provide path to a maven project in argument to this program.");
            return;
        }

        File mavenProject = new File(args[0]);

        if (!mavenProject.exists() || !mavenProject.isDirectory() || !FileUtils.getFile(mavenProject, "pom.xml").exists()) {
            log.error("Please provide a valid path to a maven project in argument to this program.");
            return;
        }

        log.info("Start Dynamic Analysis of {}", mavenProject.getAbsolutePath());

        File classesFolder = FileUtils.getFile(mavenProject, "target/classes");
//        File srcClassesFolder = FileUtils.getFile(mavenProject, "src/main/java");
        File testClassesFolder = FileUtils.getFile(mavenProject, "target/test-classes");
//        File srcTestClassesFolder = FileUtils.getFile(mavenProject, "src/test/java");

        if (!testClassesFolder.exists() || !classesFolder.exists()) {
            log.warn("There is no compiled test to run or no compiles source code to test.");
            return;
        }

        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[]{testClassesFolder.toURI().toURL(), classesFolder.toURI().toURL()});

            Collection<File> testSuites = FileUtils.listFiles(testClassesFolder, new String[]{"class"}, true);

            log.info("Found {} test suites to run", testSuites.size());
            ClassPool pool = ClassPool.getDefault();

            try {
                pool.appendClassPath(classesFolder.getPath());
                pool.appendClassPath(testClassesFolder.getPath());
            } catch (NotFoundException e) {
                e.printStackTrace();
            }

            Loader loader = new Loader(classLoader, pool);

            loader.delegateLoadingOf("org.junit.");

            try {
                loader.addTranslator(pool, new Translator() {
                    @Override
                    public void start(ClassPool pool) throws NotFoundException, CannotCompileException {
                    }

                    @Override
                    public void onLoad(ClassPool pool, String classname) throws NotFoundException, CannotCompileException {
                        if(pool.find(classname).getPath().startsWith(testClassesFolder.getAbsolutePath())) {
                            //It is a test class
                            return;
                        }

                        log.info("[JAVASSIST] {}", classname);
                    }
                });
            } catch (NotFoundException e) {
                e.printStackTrace();
            } catch (CannotCompileException e) {
                e.printStackTrace();
            }



            for (File testSuite : testSuites) {
                ClassFile classFile = new ClassFile(new DataInputStream(new FileInputStream(testSuite.getAbsolutePath())));
                log.debug("TestSuite ClassName: {}", classFile.getName());
                loader.delegateLoadingOf("org.junit.");

                Result result = JUnitCore.runClasses(loader.loadClass(classFile.getName()));

                if (!result.wasSuccessful()) {
                    log.warn("{} Test Failed out of {}", result.getFailureCount(), result.getRunCount());

                    for(Failure failure : result.getFailures()) {
                        log.warn("Failed for: {} with trace: {}", failure.toString(), failure.getTrace());
                    }
                } else {
                    log.info("All Test Succeed");
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

//    public static void main( String[] args )
//    {
//        try {
//            ClassPool pool = ClassPool.getDefault();
//
//            MetadataClassGenerator generator = new MetadataClassGenerator(pool);
//
//            //CtClass singleton = generator.generateSingleton();
//
//            Loader loader = new Loader(pool);
//            Translator logger = new Translator() {
//                public void start(ClassPool classPool) throws NotFoundException, CannotCompileException {
//                    System.out.println("Starting");
//                }
//
//                public void onLoad(ClassPool classPool, String s) throws NotFoundException, CannotCompileException {
//                    log.info(s);
//
//                    ClassHandler handler = new BlockClassHandler(classPool.get(s));
//                    handler.handle();
//                }
//            };
//
//            loader.addTranslator(pool, logger);
//
//            //singleton.writeFile("input/target/classes");
//            pool.appendClassPath("input/target/classes");
//
//
//            loader.run("m2.vv.tutorials.QuotesApp", args);
//        }
//
//        catch(VerifyError err) {
//            log.error(err.getMessage());
//        }
//
//        catch(Throwable exc) {
//            System.out.println("An error occured");
//            System.out.println(exc.getMessage());
//            exc.printStackTrace();
//        }
//    }

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
