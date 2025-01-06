package moe.dituon.petpet.bot.qq.onebot;

import cn.evolvefield.onebot.client.core.Bot;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.dituon.petpet.bot.qq.QQBotService;
import moe.dituon.petpet.core.utils.image.EncodedImage;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class OnebotBotService extends QQBotService {
    @Getter
    public final OnebotConfig onebotConfig;
    @Getter
    public final Bot bot;
    protected final BaseHttpServer httpServer;
    public static final int DEFAULT_HTTP_SERVER_PORT = 2233;
    public static final String DEFAULT_HTTP_SERVER_URL = "http://localhost:" + DEFAULT_HTTP_SERVER_PORT + "/";

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

    public String putImage(EncodedImage image) {
        var id = UUID.randomUUID().toString();
        httpServer.cache.put(id, image);
        return onebotConfig.getHttpServerUrl() + id;
    }
}
