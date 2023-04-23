package moe.dituon.petpet.server;

import moe.dituon.petpet.share.BasePetService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ServerPetService extends BasePetService {
    public static final int DEFAULT_PORT = 2333;
    public static final String DEFAULT_DATA_PATH = "data/xmmt.dituon.petpet/";
    public static final String CONFIG_NAME = "server-config.json";
    public static final int DEFAULT_SERVER_THREAD_POOL_SIZE = 10;
    public int port = DEFAULT_PORT;
    public String path = DEFAULT_DATA_PATH;
    public int webServerThreadPoolSize = DEFAULT_SERVER_THREAD_POOL_SIZE;
    public boolean usePreview = false;
    private String indexJson;

    public void readConfig(ServerServiceConfig config) {
        port = config.getPort();
        path = config.getDataPath();
        webServerThreadPoolSize = config.getWebServerThreadPoolSize();
        usePreview = config.getPreview();

        readBaseServiceConfig(config.toBaseServiceConfig());
        LOGGER.info("GifMakerThreadPoolSize: " + super.getGifEncoderThreadPoolSize());
    }

    public void readConfig() {
        File configFile = new File("./" + CONFIG_NAME);
        try {
            if (!configFile.exists()) { //save default config
                var defaultConfig = new ServerServiceConfig();
                defaultConfig.setPreview(usePreview);
                Files.write(configFile.toPath(), defaultConfig.stringify().getBytes());
            }

            ServerServiceConfig config = ServerServiceConfig.parse(getFileStr(configFile));
            readConfig(config);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void readData(File[] files) {
        super.readData(files);
        indexJson = PetDataDTO.stringify(super.dataMap);
        LOGGER.info("\n---Key List---\n" + keyListString + "\n--------------\n");
    }

    public String getIndexJson() {
        return indexJson;
    }
}
