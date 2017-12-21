package fr.istic.vnv;

import fr.istic.vnv.instrumentation.ClassInstrumenter;
import fr.istic.vnv.report.ReportGenerator;
import fr.istic.vnv.report.ReportGeneratorFactory;
import fr.istic.vnv.utils.Config;
import fr.istic.vnv.utils.ExtendedTextListener;
import javassist.*;
import javassist.bytecode.ClassFile;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 *
 */
public class App {
    public static final PrintStream syserr = new PrintStream(new FileOutputStream(FileDescriptor.err));


    private static final String
            vnvDistantFolder = "target/vnv/",
            dependencyFolder = vnvDistantFolder + "vnv-dependencies/",
            analysisSaveFolder = vnvDistantFolder + "vnv-analysis/";

    private static Logger log = LoggerFactory.getLogger(App.class);

    public static ClassPool pool;

    private File projectDirectory;
    private MavenProject mavenProject;

    private File classesFolder, testClassesFolder;
    private Loader loader;
    private ReportGenerator generator;

    /**
     * Initialize all the project analyse
     * @param projectDirectory path of a maven project to analyse
     * @throws IOException Path doesn't exist
     * @throws XmlPullParserException
     */
    public App(File projectDirectory) throws IOException, XmlPullParserException {
        this.projectDirectory = projectDirectory;

        try {
            if(Config.get().doOutputRedirection() && log.isInfoEnabled() && !log.isDebugEnabled()) {//Redirect output only in info logging mode if environment is set
                File vnvFolder = FileUtils.getFile(projectDirectory, vnvDistantFolder);

                if(!vnvFolder.exists()) {
                    FileUtils.forceMkdir(vnvFolder);
                }

                File output = FileUtils.getFile(vnvFolder, "out.txt");
                File errput = FileUtils.getFile(vnvFolder, "err.txt");
                output.createNewFile();
                errput.createNewFile();
                System.setOut(new PrintStream(output));
                System.setErr(new PrintStream(errput));
            }
        } catch (IOException e) {
            log.error("Impossible to redirect standards output into out.txt and err.txt");
            if(log.isDebugEnabled())
                e.printStackTrace(syserr);
        }

        MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
        Model model = mavenXpp3Reader.read(new FileInputStream(FileUtils.getFile(projectDirectory, "pom.xml")));
        mavenProject = new MavenProject(model);

        this.generator = ReportGeneratorFactory.getTextReportGenerator(FileUtils.getFile(projectDirectory, analysisSaveFolder));
    }

    /**
     * Add the dependencies of the project to instrument to your project
     * @throws InterruptedException
     * @throws IOException
     */
    public void  buildDependencies() throws InterruptedException, IOException {
        log.info("Generating Dependencies into {}... (it can take a few seconds)", FileUtils.getFile(projectDirectory, dependencyFolder).getAbsolutePath());
        Process generatingDependenciesProcess = Runtime.getRuntime().exec("mvn dependency:copy-dependencies -DoutputDirectory=" + dependencyFolder, null,  projectDirectory);
        generatingDependenciesProcess.waitFor();
        log.info("...Done");
    }

    /**
     * Initialize all the tests of the project to analyse
     * @return
     * @throws FileNotFoundException
     */
    private Collection<File> initializeTestRuns() throws FileNotFoundException {
        log.info("Start Dynamic analysis of {}", projectDirectory.getAbsolutePath());

        classesFolder = FileUtils.getFile(projectDirectory, "target/classes");
        testClassesFolder = FileUtils.getFile(projectDirectory, "target/test-classes");

        if (!testClassesFolder.exists() || !classesFolder.exists()) {
            log.warn("There is no compiled test to run or no compiles source code to test.");
            throw new FileNotFoundException(testClassesFolder.getAbsolutePath() + " or " + classesFolder.getAbsolutePath() + " are nowhere to be found !");
        }

        Collection<File> testSuites = FileUtils.listFiles(testClassesFolder, new String[]{"class"}, true);

        Stream<File> fileStream = testSuites.stream();

        testSuites = fileStream.filter(file -> !file.getName().contains("$")).filter(file -> file.getName().endsWith("Test.class")).collect(Collectors.toCollection(ArrayList::new));

        log.debug("Found {} test suites to run", testSuites.size());
        pool = ClassPool.getDefault();
        pool.importPackage("java.io");

        try {
            pool.appendClassPath(classesFolder.getPath());
            pool.appendClassPath(testClassesFolder.getPath());

            for(File jarFile : FileUtils.listFiles(FileUtils.getFile(projectDirectory, dependencyFolder), new String[]{"jar"}, true)) {
                pool.appendClassPath(jarFile.getAbsolutePath());
            }
        } catch (NotFoundException e) {
            if(log.isDebugEnabled())
                e.printStackTrace(syserr);
        }

        return testSuites;
    }

    /**
     * Initialize Loader
     * - Delegate loading of packagename to not instrument
     * - Add Translator to update bytecode being executed
     */
    private void initializeJavassistLoader() throws IOException {

        List<URL> classToLoad = new ArrayList<>();

        classToLoad.add(testClassesFolder.toURI().toURL());
        classToLoad.add(classesFolder.toURI().toURL());


        for(File jarFile : FileUtils.listFiles(FileUtils.getFile(projectDirectory, dependencyFolder), new String[]{"jar"}, true)) {
            String urlPath = "jar:file:" + jarFile.getAbsolutePath() + "!/";
            classToLoad.add(new URL(urlPath));
            log.debug("Adding {} to ClassLoader", jarFile.getCanonicalPath());
        }

        URLClassLoader classLoader = URLClassLoader.newInstance(classToLoad.toArray(new URL[classToLoad.size()]));


        loader = new Loader(classLoader, pool);

        /*
         * Do not instrument the folowing packages:
         * (by default Javassist do not instrument java.lang.* and other native methods)
         */
        loader.delegateLoadingOf("org.junit.");
        loader.delegateLoadingOf("junit.");
        loader.delegateLoadingOf("javassist.");
        loader.delegateLoadingOf("fr.istic.vnv.");

        for (int i = 0; i < mavenProject.getDependencies().size(); i++) {
            Dependency dependency = (Dependency) mavenProject.getDependencies().get(i);

            if(!mavenProject.getGroupId().equals(dependency.getGroupId()) && !mavenProject.getGroupId().equals(mavenProject.getArtifactId())) {
                loader.delegateLoadingOf(dependency.getGroupId() + ".");
                log.info("Delegate loading of {}", dependency.getGroupId() + ".");
            }
        }

        if(Config.get().doExecutionTrace() || Config.get().doBranchCoverage()) {
            try {
                loader.addTranslator(pool, getApplicationTransaltor());
            } catch (NotFoundException | CannotCompileException e) {
                log.error("Unable to instrument some method due to: {}", e.getMessage());

                if (log.isDebugEnabled())
                    e.printStackTrace(syserr);
            }
        }
    }

    /**
     *
     * @return
     */
    private Translator getApplicationTransaltor() {
        return new Translator() {
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
                            e.printStackTrace(syserr);
                    }

                } catch (IOException e) {
                    log.error("Unable to define if {} is in {} or not", classLocationPath, testClassesFolder.getPath());

                    if(log.isDebugEnabled())
                        e.printStackTrace(syserr);
                }
            }
        };
    }

    /**
     * Run a list of tests
     * @param testSuites
     * @throws ClassNotFoundException
     * @throws InitializationError
     * @throws IOException
     */
    private void runTests(Collection<File> testSuites) throws ClassNotFoundException, InitializationError, IOException {
        List<Class> classesToTest = new ArrayList<>();

        for (File testSuite : testSuites) {
            ClassFile classFile = new ClassFile(new DataInputStream(new FileInputStream(testSuite.getAbsolutePath())));

            if(classFile.isAbstract()) {
                log.trace("Abstract Test Class found: {}, it will be ignored", classFile.getName());
                continue;
            }
            log.trace("Add Test Class to be runned: {}", classFile.getName());

            classesToTest.add(loader.loadClass(classFile.getName()));

        }

        Suite suite = new Suite(new AllDefaultPossibilitiesBuilder(true), classesToTest.toArray(new Class[classesToTest.size()]));

        RunNotifier notifier = new RunNotifier();

        ExtendedTextListener listener = new ExtendedTextListener(generator, suite.testCount());
        notifier.addListener(listener);
        suite.run(notifier);

        log.info("Runned {} tests, {} have failed", suite.testCount(), listener.getFailuresCount());

        generator.save();
        log.info("report successfully saved in {} !", FileUtils.getFile(projectDirectory, analysisSaveFolder).getAbsolutePath());
    }

    /**
     *
     * @throws IOException
     */
    public void run() throws IOException {

        Collection<File> testSuites = initializeTestRuns();

        initializeJavassistLoader();

        try {
            runTests(testSuites);
        } catch (ClassNotFoundException | InitializationError | IOException e) {
            log.warn("Exception {}", e.getMessage());
            log.error("An exception occured during analyses, please check git issues and create one if there is none of that kind at http://www.github.com/tanaht/VNVProject");

            if(log.isDebugEnabled())
                e.printStackTrace(syserr);
        }
    }
    public static void main(String[] args) {
        if (args.length != 1) {
            log.error("Please provide path to a maven project in argument to this program.");
            return;
        }

        File projectDirectory = new File(args[0]);

        if (!projectDirectory.exists() || !projectDirectory.isDirectory() || !FileUtils.getFile(projectDirectory, "pom.xml").exists()) {
            log.error("Please provide a valid path to a maven project in argument to this program.");
            return;
        }

        App application = null;
        try {
            application = new App(projectDirectory);
        } catch(IOException | XmlPullParserException e) {
            log.error("Unable to read properly file 'pom.xml' into {} perhaps it is corrupted ?", projectDirectory.getAbsolutePath());

            e.printStackTrace(syserr);
            return;
        }

        try {
            application.buildDependencies();
        } catch(IOException | InterruptedException e) {
            log.error(e.getMessage());

            if(log.isDebugEnabled())
                e.printStackTrace(syserr);
        }

        try {
            application.run();
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        } catch (Exception e) {
            log.error("Unknown Exception {}", e.getMessage());

            e.printStackTrace(syserr);
        }

        System.exit(0);
    }
}
