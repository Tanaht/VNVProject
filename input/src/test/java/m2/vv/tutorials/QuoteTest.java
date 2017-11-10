package m2.vv.tutorials;

import org.junit.Assert;
import org.junit.Test;

public class QuoteTest {

    @Test
    public void testConstructor() {
        Quote quote = new Quote("author", "text");

        Assert.assertEquals("author", quote.author);
        Assert.assertEquals("text", quote.text);
    }

    @Test
    public void testGetter() {
        Quote quote = new Quote("author", "text");

        Assert.assertEquals("author", quote.getAuthor());
        Assert.assertEquals("text", quote.getText());
    }
}
