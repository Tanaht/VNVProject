package fr.istic.vnv;

import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 */
public class App 
{
    private static Logger log = LoggerFactory.getLogger(App.class);

    public static void main( String[] args )
    {
        try {
            ClassPool pool = ClassPool.getDefault();
            Loader loader = new Loader(pool);

            Translator logger = new Translator() {
                public void start(ClassPool classPool) throws NotFoundException, CannotCompileException {
                    System.out.println("Starting");
                }

                public void onLoad(ClassPool classPool, String s) throws NotFoundException, CannotCompileException {
                    log.info(s);

                    ClassHandler handler = new ClassHandler(classPool.get(s));
                    handler.handle();
                }
            };

            loader.addTranslator(pool, logger);

            pool.appendClassPath("input/target/classes");


            loader.run("m2.vv.tutorials.QuotesApp", args);
        }

        catch(VerifyError err) {
            log.error(err.getMessage());
        }

        catch(Throwable exc) {
            System.out.println("An error occured");
            System.out.println(exc.getMessage());
            exc.printStackTrace();
        }
    }
}
