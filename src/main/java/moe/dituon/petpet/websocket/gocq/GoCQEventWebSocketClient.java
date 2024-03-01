package moe.dituon.petpet.websocket.gocq;

import kotlinx.serialization.json.Json;
import kotlinx.serialization.json.JsonObject;
import moe.dituon.petpet.plugin.PluginPetService;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Objects;

public class GoCQEventWebSocketClient extends WebSocketClient {
    public GoCQEventWebSocketClient(URI serverURI) {
        super(serverURI);
        connect();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        PluginPetService.LOGGER.info("GoCQ API WebSocket 连接成功");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        PluginPetService.LOGGER.info("GoCQ API WebSocket 连接关闭 (" + code + "): " + reason);
    }

    @Override
    public void onError(Exception ex) {
        PluginPetService.LOGGER.warning("an error occurred:" + ex);
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonObject obj = (JsonObject) Json.Default.parseToJsonElement(message);
            String type = Objects.requireNonNull(obj.get("post_type")).toString();
            if (!"\"message\"".equals(type)) return;
            GoCQGroupMessageEventDTO e = GoCQGroupMessageEventDTO.parse(obj);
            new OneBotGroupMessage(e);
        } catch (Exception e) {
            PluginPetService.LOGGER.warning(message, e);
        }
    }
}