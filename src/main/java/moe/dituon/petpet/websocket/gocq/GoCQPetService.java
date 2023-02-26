package moe.dituon.petpet.websocket.gocq;

import moe.dituon.petpet.plugin.Cooler;
import moe.dituon.petpet.plugin.DataUpdater;
import moe.dituon.petpet.plugin.PluginPetService;
import moe.dituon.petpet.server.WebServer;
import moe.dituon.petpet.share.TextExtraData;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class GoCQPetService extends PluginPetService {
    public static final String EVENT_WEBSOCKET_URL = "ws://127.0.0.1:8080";
    public static final String API_WEBSOCKET_URL = "ws://127.0.0.1:8080/api";
    public static final String CONFIG_NAME = "gocq-config.json";
    public String eventWebSocketUri = EVENT_WEBSOCKET_URL;
    public String apiWebSocketUri = API_WEBSOCKET_URL;
    public Long coolDown = Cooler.DEFAULT_USER_COOLDOWN;
    public Long groupCoolDown = Cooler.DEFAULT_GROUP_COOLDOWN;
    public String inCoolDownMessage = Cooler.DEFAULT_MESSAGE;
    public boolean autoUpdate = true;
    public String repositoryUrl = DataUpdater.DEFAULT_REPO_URL;

    private WebServer server;

    public void readConfig() {
        File configFile = new File(CONFIG_NAME);
        try {
            if (!configFile.exists()) { //save default config
                Files.write(Paths.get(CONFIG_NAME), new GoCQPluginConfig().stringify().getBytes());
            }

            GoCQPluginConfig config = GoCQPluginConfig.parse(getFileStr(configFile));
            eventWebSocketUri = config.getEventWebSocketUrl();
            apiWebSocketUri = config.getApiWebSocketUrl();

            autoUpdate = config.getAutoUpdate();
            repositoryUrl = config.getRepositoryUrl();
            coolDown = config.getCoolDown();
            groupCoolDown = config.getGroupCoolDown();
            inCoolDownMessage = config.getInCoolDownMessage().isBlank() ?
                    null : config.getInCoolDownMessage();

            readPluginServiceConfig(config.toPluginServiceConfig());
            super.dataRoot = new File(config.getDataPath());

            server = new WebServer(config.toServerServiceConfig());
            super.gifMaker = server.service.getGifMaker();
            super.imageMaker = server.service.getImageMaker();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void readData() {
        super.dataMap = server.service.getDataMap();
        super.aliaMap = server.service.getAliaMap();
        super.keyListString = server.service.keyListString;
    }

    public void sendImage(long groupId,
                          String key,
                          AvatarUrls avatars,
                          TextExtraData textExtraData) {
        GoCQPetpet.getInstance().apiClient.send(new GoCQSendMessageRequestParamDTO(
                groupId,
                server.getApiUrl() + "?key=" + key + avatars.getURL() + buildTextExtraDataUrl(textExtraData)
        ).toRequestDTO().stringify());
    }

    private static String buildTextExtraDataUrl(TextExtraData data) {
        return "&fromName=" + URLEncoder.encode(data.getFromReplacement(), StandardCharsets.UTF_8) +
                "&toName=" + URLEncoder.encode(data.getToReplacement(), StandardCharsets.UTF_8) +
                "&groupName=" + URLEncoder.encode(data.getFromReplacement(), StandardCharsets.UTF_8) +
                "&textList=" + data.getTextList().stream()
                .map(span -> URLEncoder.encode(span, StandardCharsets.UTF_8))
                .collect(Collectors.joining(" "));
    }

    protected static class AvatarUrls {
        String from, to, group, bot;

        AvatarUrls(String from, String to, String group, String bot) {
            this.from = URLEncoder.encode(from, StandardCharsets.UTF_8);
            this.to = URLEncoder.encode(to, StandardCharsets.UTF_8);
            this.group = URLEncoder.encode(group, StandardCharsets.UTF_8);
            this.bot = URLEncoder.encode(bot, StandardCharsets.UTF_8);
        }

        public String getURL() {
            return "&fromAvatar=" + from +
                    "&toAvatar=" + to +
                    "&groupAvatar=" + group +
                    "&botAvatar=" + bot +
                    "&randomAvatarList=" + from;
        }
    }
}
