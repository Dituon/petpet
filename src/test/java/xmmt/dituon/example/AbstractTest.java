package xmmt.dituon.example;

import kotlin.Pair;
import org.junit.BeforeClass;
import xmmt.dituon.share.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class AbstractTest {

    static BasePetService petService = new BasePetService();
    static final int DEFAULT_BUFFER_SIZE = 8192;
    static final String INPUT_ROOT = "./example-data/input/";
    static final String OUTPUT_ROOT = "./example-data/output/";
    static BufferedImage avatarImage1;
    static BufferedImage avatarImage2;



    @BeforeClass
    public static void baseInit() {
        try {
            avatarImage1 = ImageIO.read(new File(INPUT_ROOT + "avatar1.png"));
            avatarImage2 = ImageIO.read(new File(INPUT_ROOT + "avatar2.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void testGeneral(String saveName, String key, List<TextData> additionTextDatas) {
        Pair<InputStream, String> resultStreamAndType = petService.generateImage(key,
                new AvatarExtraDataProvider(() -> avatarImage1, () -> avatarImage2,
                        null, null)
                , null, additionTextDatas);
        String finalSaveName = OUTPUT_ROOT + getClass().getSimpleName() + "-" + saveName + "." + resultStreamAndType.getSecond();
        copyInputStreamToFile(resultStreamAndType.getFirst(), new File(finalSaveName));
        System.out.println("test " + key + " done.");
    }

    private void copyInputStreamToFile(InputStream inputStream, File file) {
        // append = false
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
