import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MyVeryFirstTest {

    @Test
    public void toUpperCaseConvertsAllLetterstoUppercase() {

        // given
        String testString = "kleinGROSS";

        // when
        String result = testString.toUpperCase();

        // then
        Assertions.assertEquals("KLEINGROSS", result);

    }


}
