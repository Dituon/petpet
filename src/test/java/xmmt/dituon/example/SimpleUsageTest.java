package xmmt.dituon.example;

import kotlin.Pair;
import org.junit.BeforeClass;
import org.junit.Test;
import xmmt.dituon.share.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class SimpleUsageTest {

    static BasePetService petService = new BasePetService();
    static final int DEFAULT_BUFFER_SIZE = 8192;
    static final String EXAMPLE_ROOT = "./example-data/";
    static final String INPUT_ROOT = "./example-data/input/";
    static final String OUTPUT_ROOT = "./example-data/output/";
    static BufferedImage avatarImage1;
    static BufferedImage avatarImage2;

    @BeforeClass
    public static void init() {
        try {
            avatarImage1 = ImageIO.read(new File(INPUT_ROOT + "avatar1.png"));
            avatarImage2 = ImageIO.read(new File(INPUT_ROOT + "avatar2.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        BaseServiceConfig config = new BaseServiceConfig();
        petService.readBaseServiceConfig(config);
        petService.readData(new File(EXAMPLE_ROOT + "./data"));
    }


    @Test
    public void testPandaFace() throws IOException {
        testGeneral("testPandaFace", "panda-face", null, Arrays.asList(textDataForPandaFace("。。。")));
    }

    @Test
    public void testPandaFace2() throws IOException {
        testGeneral("testPandaFace2", "panda-face", null, Arrays.asList(textDataForPandaFace("二次元，二次元")));
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

    @Test
    public void testOsu() throws IOException {
        testGeneral("testOsu", "osu", new TextExtraData("", "", "", Arrays.asList("测试！")), null);
    }

    @Test
    public void testPetpet() throws IOException {
        testGeneral("testPetpet", "petpet", null, null);
    }

    @Test
    public void testKiss() throws IOException {
        testGeneral("testKiss", "kiss", null, null);
    }

    private void testGeneral(String saveName, String key, TextExtraData textExtraData, List<TextData> additionTextDatas) throws IOException {
        Pair<InputStream, String> resultStreamAndType = petService.generateImage(avatarImage1, avatarImage2, key, textExtraData, additionTextDatas);
        copyInputStreamToFile(resultStreamAndType.getFirst(), new File(OUTPUT_ROOT + saveName + "." + resultStreamAndType.getSecond()));
        System.out.println("test " + key + " done.");
    }

    private void copyInputStreamToFile(InputStream inputStream, File file)
            throws IOException {
        // append = false
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }


}
