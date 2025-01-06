import moe.dituon.petpet.core.length.Length;
import org.junit.jupiter.api.Test;

public class LengthTest {
    @Test
    void testSplitLengthTokens() {
        var testStrings = new String[]{
                "0 0",
                "100px 200px calc(100vw - 20px) 400px",
                "0 400px calc(1) 20ch"
        };
        var resultTokens = new String[][] {
                {"0", "0"},
                {"100px", "200px", "calc(100vw - 20px)", "400px"},
                {"0", "400px", "calc(1)", "20ch"}
        };

        for (int i = 0; i < testStrings.length; i++) {
            var tokens = Length.splitString(testStrings[i]);
            try {
                assert tokens.size() == resultTokens[i].length;
                for (int j = 0; j < tokens.size(); j++) {
                    assert tokens.get(j).equals(resultTokens[i][j]);
                }
            } catch (AssertionError e) {
                System.err.println(testStrings[i] + " failed: " + tokens);
                throw e;
            }
        }
    }
}
