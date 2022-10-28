package xmmt.dituon.example;

import kotlin.Pair;
import org.junit.BeforeClass;
import org.junit.Test;
import moe.dituon.petpet.share.BaseConfigFactory;
import moe.dituon.petpet.share.BaseServiceConfig;
import moe.dituon.petpet.share.TextData;
import moe.dituon.petpet.share.TextExtraData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class ExperimentalDataServiceTest extends AbstractServiceTest {

    @BeforeClass
    public static void init() {
        BaseServiceConfig config = new BaseServiceConfig();
        petService.readBaseServiceConfig(config);
        petService.readData(new File("./data/xmmt.dituon.petpet").listFiles());
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

    public void testAll() {
        TextExtraData textExtraData = new TextExtraData(
                "恋恋", "绝绝", "幻想乡",
                List.of("petpet!")
        );
        petService.getDataMap().keySet().forEach(key -> {
            Pair<InputStream, String> resultStreamAndType = petService.generateImage(
                    key,
                    BaseConfigFactory.getGifAvatarExtraDataFromUrls(
                            "file:/example-data/input/from.gif",
                            "file:/example-data/input/to.gif",
                            null, null
                    )
                    , textExtraData, null
            );
            final String saveName = OUTPUT_ROOT + key + '.' + resultStreamAndType.getSecond();
            copyInputStreamToFile(resultStreamAndType.getFirst(), new File(saveName));
        });
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
