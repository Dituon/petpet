package xmmt.dituon.example;

import org.junit.BeforeClass;
import xmmt.dituon.share.AvatarExtraDataProvider;
import xmmt.dituon.share.BasePetService;
import xmmt.dituon.share.TextData;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public abstract class AbstractTest {
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

    protected void copyInputStreamToFile(InputStream inputStream, File file) {
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
