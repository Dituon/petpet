package moe.dituon.petpet.httpserver;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.dituon.petpet.service.ObservableBaseService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Collections;

@Slf4j
public class ServerService extends ObservableBaseService {
    protected String infoCache = null;
    protected int previousVersion = super.updateVersion;
    public static final File CONFIG_FILE = new File("http-server.json");
    @Getter
    public final ServerServiceConfig config;

    public ServerService() {
        this.config = loadConfig();
    }

    public ServerService(ServerServiceConfig config) {
        this.config = config;
    }

    protected static ServerServiceConfig loadConfig() {
        ServerServiceConfig loadedConfig = null;

        if (CONFIG_FILE.exists()) {
            try {
                String content = Files.readString(CONFIG_FILE.toPath());
                loadedConfig = ServerServiceConfig.fromJsonString(content);
            } catch (Exception e) {
                File backupFile = new File(CONFIG_FILE.getParentFile(), CONFIG_FILE.getName() + ".old_" + Instant.now().toEpochMilli() + ".bak");
                try {
                    Files.copy(CONFIG_FILE.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    log.warn("读取配置文件错误，已保存旧文件到 {}", backupFile.getName(), e);
                } catch (IOException ioException) {
                    log.error("无法保存旧配置文件的备份", ioException);
                }
            }
        }

        if (loadedConfig == null) {
            loadedConfig = new ServerServiceConfig();
        }

        try {
            String newContent = loadedConfig.toJsonString();
            Files.writeString(CONFIG_FILE.toPath(), newContent);
        } catch (IOException e) {
            log.error("无法写入配置文件", e);
        }

        return loadedConfig;
    }

    public String getInfo() {
        if (super.updateVersion != this.previousVersion) {
            synchronized (this) {
                if (super.updateVersion != this.previousVersion) {
                    this.infoCache = ServerInfo.fromModelMap(this.staticModelMap).toJsonString();
                    this.previousVersion = super.updateVersion;
                }
            }
        }
        if (this.infoCache == null) {
            this.infoCache = ServerInfo.fromModelMap(Collections.emptyMap()).toJsonString();
        }
        return this.infoCache;
    }
}
