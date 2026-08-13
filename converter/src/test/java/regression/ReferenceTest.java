
package regression;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;

public class ReferenceTest {

    private int value;

    @Before
    public void setup() {
        this.value = 10;
    }

    @Test
    public void testAddition() {
        Assert.assertEquals(15, value + 5);
    }

    @Test
    public void testMultiplication() {
        Assert.assertEquals(20, value * 2);
    }
}