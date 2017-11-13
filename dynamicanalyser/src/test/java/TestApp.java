import fr.istic.vnv.App;
import org.junit.Assert;
import org.junit.Test;

public class TestApp {
    @Test
    public void testHelloWorld() {
        String actual = App.helloWorld();
        Assert.assertEquals("Hello World", actual);
    }


    @Test
    public void testHelloWhat() {
        String actual = App.helloWhat(true);
        Assert.assertEquals("Hello True", actual);
    }
}
