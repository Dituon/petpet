package xmmt.dituon.example;

import org.junit.BeforeClass;
import org.junit.Test;
import xmmt.dituon.share.BasePetService;
import xmmt.dituon.share.ConfigDTO;
import xmmt.dituon.share.ConfigDTOKt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
        ConfigDTO configDTO = getConfigFromFile(new File(EXAMPLE_ROOT + "config/petpet.json"));
        petService.readConfig(configDTO);
        petService.readData(new File(EXAMPLE_ROOT + "./data"));
    }


    @Test
    public void testNijigen() throws IOException {
        InputStream resultStream = petService.generateImage(avatarImage1, avatarImage2, "nijigen");
        copyInputStreamToFile(resultStream, new File(OUTPUT_ROOT + "nijigen.png"));
        System.out.println("testNijigen done.");
    }

    @Test
    public void testPetpet() throws IOException {
        InputStream resultStream = petService.generateImage(avatarImage1, avatarImage2, "petpet");
        copyInputStreamToFile(resultStream, new File(OUTPUT_ROOT + "testPetpet.gif"));
        System.out.println("testPetpet done.");
    }

    @Test
    public void testKiss() throws IOException {
        InputStream resultStream = petService.generateImage(avatarImage1, avatarImage2, "kiss");
        copyInputStreamToFile(resultStream, new File(OUTPUT_ROOT + "testKiss.gif"));
        System.out.println("testKiss done.");
    }

    /** 如果要使用文字构造方法，请在generateImage后接String数组
     * String[0] 对应 $from
     * String[1] 对应 $to
     * String[2] 对应 $group
     * 使用例:
     * petService.generateImage(avatarImage1, avatarImage2, "key", new String[]{"$from", "$to", "$group"});
     */


    private void copyInputStreamToFile(InputStream inputStream, File file)
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


    private static ConfigDTO getConfigFromFile(File configFile) {

            if (configFile.exists()) {
                try {
                    return ConfigDTOKt.decode(petService.getFileStr(configFile));
                } catch (IOException e) {
                    e.printStackTrace();
                    return createConfig(configFile);
                }
            } else {
                return createConfig(configFile);
            }

    }

    private static ConfigDTO createConfig(File configFile) {
        ConfigDTO configDTO = new ConfigDTO();
        try {
            String defaultConfig = ConfigDTOKt.encode(configDTO);
            if (!configFile.createNewFile()) {
                System.out.print("正在写入新版本配置文件");
            }
            FileOutputStream defaultConfigOS = new FileOutputStream(configFile);
            defaultConfigOS.write(defaultConfig.getBytes(StandardCharsets.UTF_8));
            System.out.println("写入配置文件成功，路径: Mirai/plugins/petpet.json");
        } catch (IOException ex) {
            System.out.println("无法写入配置文件，请检查文件路径!");
        }
        return configDTO;
    }


}
