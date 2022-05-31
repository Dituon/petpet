package xmmt.dituon.example;

import xmmt.dituon.share.BasePetData;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SimpleUsage {

    static final int DEFAULT_BUFFER_SIZE = 8192;
    static final String INPUT_ROOT = "./example-data/input/";
    static final String OUTPUT_ROOT = "./example-data/output/";
    static BufferedImage avatarImage1;
    static BufferedImage avatarImage2;
    static {
        try {
            avatarImage1 = ImageIO.read(new File(INPUT_ROOT + "avatar1.png"));
            avatarImage2 = ImageIO.read(new File(INPUT_ROOT + "avatar2.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        try {
            BasePetData.readConfig(new File("./config/xmmt.dituon.petpet/petpet.json"));
            BasePetData.readData(new File("./data/xmmt.dituon.petpet"));

            testPetpet();
            testBite();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void testPetpet() throws IOException {
        InputStream resultStream = BasePetData.generateImage(avatarImage1, avatarImage2, "petpet");
        copyInputStreamToFile(resultStream, new File(OUTPUT_ROOT + "testPetpet.gif"));
    }

    private static void testBite() throws IOException {
        InputStream resultStream = BasePetData.generateImage(avatarImage1, avatarImage2, "bite");
        copyInputStreamToFile(resultStream, new File(OUTPUT_ROOT + "testBite.gif"));
    }

    private static void copyInputStreamToFile(InputStream inputStream, File file)
            throws IOException {
        // append = false
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }

    }

}
