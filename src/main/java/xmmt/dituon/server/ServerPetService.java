package xmmt.dituon.server;

import xmmt.dituon.share.BasePetService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerPetService extends BasePetService {
    public int port = 2333;
    public int threadPoolSize = 10;
    public boolean headless = true;

    public void readConfig() {
        File configFile = new File("config.yml");
        try {
            if (!configFile.exists()) { //save default config
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("config.yml");
                assert is != null;
                Files.write(Paths.get("config.yml"), is.readAllBytes());
            }
            String configStr = getFileStr(configFile);

            Matcher portRegex = Pattern.compile("port\\s*:\\s*(\\d+)").matcher(configStr);
            if (portRegex.find()) port = Integer.parseInt(portRegex.group(1));

            Matcher threadPoolSizeRegex = Pattern.compile("hreadPoolSize\\s*:\\s*(\\d+)").matcher(configStr);
            if (threadPoolSizeRegex.find()) threadPoolSize = Integer.parseInt(threadPoolSizeRegex.group(1));

            Matcher headlessRegex = Pattern.compile("headless\\s*:\\s*(true|false)").matcher(configStr);
            if (headlessRegex.find()) headless = headlessRegex.group(1).equals("true");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
