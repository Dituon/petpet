package xmmt.dituon.example;

import xmmt.dituon.share.BasePetService;
import xmmt.dituon.share.ConfigDTO;
import xmmt.dituon.share.ConfigDTOKt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class SimpleUsage {
    BasePetService petService = new BasePetService();
    static final int DEFAULT_BUFFER_SIZE = 8192;
    static final String INPUT_ROOT = "./example-data/input/";
    static final String OUTPUT_ROOT = "./example-data/output/";
    BufferedImage avatarImage1;
    BufferedImage avatarImage2;

    public SimpleUsage() {
        try {
            avatarImage1 = ImageIO.read(new File(INPUT_ROOT + "avatar1.png"));
            avatarImage2 = ImageIO.read(new File(INPUT_ROOT + "avatar2.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        ConfigDTO configDTO = getConfigFromFile(new File(INPUT_ROOT + "config/petpet.json"));
        petService.readConfig(configDTO);
        petService.readData(new File("./data/xmmt.dituon.petpet"));
    }



    public static void main(String[] args) {
        SimpleUsage simpleUsage = new SimpleUsage();
        try {
            simpleUsage.testPetpet();
            simpleUsage.testBite();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void testPetpet() throws IOException {
        InputStream resultStream = petService.generateImage(avatarImage1, avatarImage2, "petpet");
        copyInputStreamToFile(resultStream, new File(OUTPUT_ROOT + "testPetpet.gif"));
        System.out.println("testPetpet done.");
    }

    private void testBite() throws IOException {
        InputStream resultStream = petService.generateImage(avatarImage1, avatarImage2, "bite");
        copyInputStreamToFile(resultStream, new File(OUTPUT_ROOT + "testBite.gif"));
        System.out.println("testBite done.");
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
        }

    }


    public ConfigDTO getConfigFromFile(File configFile) {

            if (configFile.exists()) {
                try {
                    ConfigDTO config = ConfigDTOKt.decode(petService.getFileStr(configFile));
                    return config;
                } catch (IOException e) {
                    e.printStackTrace();
                    return createConfig(configFile);
                }
            } else {
                return createConfig(configFile);
            }

    }

    private ConfigDTO createConfig(File configFile) {
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
