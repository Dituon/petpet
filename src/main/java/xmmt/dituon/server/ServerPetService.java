package xmmt.dituon.server;

import xmmt.dituon.share.BasePetService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ServerPetService extends BasePetService {
    public int port = 2333;
    public int threadPoolSize = 10;
    public boolean headless = true;

    public void readConfig() {
        File configFile = new File("config.json");
        try {
            if (!configFile.exists()) { //save default config
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("config.json");
                assert is != null;
                Files.write(Paths.get("config.json"), is.readAllBytes());
            }

            ServerConfig config = ServerConfig.getConfig(getFileStr(configFile));
            port = config.getPort();
            threadPoolSize = config.getThreadPoolSize();
            headless = config.getHeadless();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
