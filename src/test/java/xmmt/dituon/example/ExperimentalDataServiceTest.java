package xmmt.dituon.example;

import org.junit.BeforeClass;
import org.junit.Test;
import xmmt.dituon.share.BaseServiceConfig;
import xmmt.dituon.share.TextData;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ExperimentalDataServiceTest extends AbstractServiceTest {

    @BeforeClass
    public static void init() {
        BaseServiceConfig config = new BaseServiceConfig();
        petService.readBaseServiceConfig(config);
        petService.readData(new File("./example-data/data"));
    }

    @Test
    public void testPandaFace() throws IOException {
        testGeneral("testPandaFace", "panda-face", Arrays.asList(textDataForPandaFace("。。。")));
    }

    @Test
    public void testPandaFace2() throws IOException {
        testGeneral("testPandaFace2", "panda-face", Arrays.asList(textDataForPandaFace("二次元，二次元")));
    }

    @Test
    public void testAnyasuki() throws IOException {
        testGeneral("testAnyasuki-deform", "anyasuki-deform", null);
    }

    private TextData textDataForPandaFace(String text) {
        int fullWidth = 500;
        int board = 50;
        int textWidth = 500 - board * 2;
        int fontSizeByHeight = 56;
        int fontSizeByWidth = (int) (textWidth * 1.0 / text.length());
        int fontSize = Math.min(fontSizeByWidth, fontSizeByHeight);
        int x = (fullWidth / 2) - (text.length() * fontSize / 2);
        return new TextData(
                text,
                Arrays.asList(x, 430),
                null,
                null,
                fontSize
        );
    }
}
