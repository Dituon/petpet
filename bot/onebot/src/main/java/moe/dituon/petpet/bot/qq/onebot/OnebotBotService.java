package moe.dituon.petpet.bot.qq.onebot;

import cn.evolvefield.onebot.client.core.Bot;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import moe.dituon.petpet.bot.TemplateExtraMetadata;
import moe.dituon.petpet.bot.qq.QQBotService;
import moe.dituon.petpet.core.utils.image.EncodedImage;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class OnebotBotService extends QQBotService {
    public static final File DEFAULT_CONFIG_FILE = new File("onebot.yml");
    public static final File DEFAULT_CUSTOM_METADATA_PATH = new File("template-metadata.yml");

    @Getter
    public final OnebotConfig onebotConfig;
    @Getter
    public final Bot bot;
    protected final BaseHttpServer httpServer;
    public static final int DEFAULT_HTTP_SERVER_PORT = 2233;
    public static final String DEFAULT_HTTP_SERVER_URL = "http://localhost:" + DEFAULT_HTTP_SERVER_PORT + "/";

    @Getter
    @Setter
    protected File customMetadataPath = DEFAULT_CUSTOM_METADATA_PATH;

    public OnebotBotService(Bot bot, OnebotConfig config) {
        super(config.toQQBotConfig());
        this.bot = bot;
        this.onebotConfig = config;
        try {
            this.httpServer = new BaseHttpServer(onebotConfig.getHttpServerPort());
        } catch (IOException e) {
            log.error("无法启动 HTTP 图像服务器");
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected Map<String, TemplateExtraMetadata> initCustomTemplateMetadata() {
        Map<String, TemplateExtraMetadata> metadataMap;
        try {
            var str = Files.readString(customMetadataPath.toPath());
            metadataMap = UtilsKt.decodeCustomMetadataConfig(str);
        } catch (Exception ex) {
            if (!(ex instanceof NoSuchFileException)) {
                log.warn("无法读取自定义模板元数据文件: {}", customMetadataPath.getAbsolutePath(), ex);
            }
            return Collections.emptyMap();
        }
        return metadataMap;
    }

    @Override
    public void updateScriptService() {
        var savedMap = buildSavedMetadataMap();
        if (savedMap.isEmpty()) {
            super.updateScriptService();
            return;
        }
        try {
            var str = UtilsKt.encodeCustomMetadataConfig(savedMap);
            Files.writeString(customMetadataPath.toPath(), str);
        } catch (IOException e) {
            log.warn("无法保存自定义模板元数据文件: {}", customMetadataPath.getAbsolutePath(), e);
        }
        super.updateScriptService();
    }

    public String putImage(EncodedImage image) {
        var id = UUID.randomUUID().toString();
        httpServer.cache.put(id, image);
        return onebotConfig.getHttpServerUrl() + id;
    }

    public boolean cacheImage(long targetId, int messageId, String imageUrl) {
        long id = getCacheId(targetId, messageId);
        this.getImageCachePool().put(id, imageUrl);
        return true;
    }

    public @Nullable String getCachedImage(long targetId, int messageId) {
        long id = getCacheId(targetId, messageId);
        return this.getImageCachePool().get(id);
    }

    /**
     * 计算消息唯一 id
     */
    public long getCacheId(long targetId, int messageId) {
        // message id = target id + source id
        return targetId + messageId;
    }
}
