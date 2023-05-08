package moe.dituon.petpet.websocket.gocq;

import moe.dituon.petpet.plugin.PluginPetService;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

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
            if(message.contains("\"post_type\":\"message\"")){
                GoCQGroupMessageEventDTO e = GoCQGroupMessageEventDTO.parse(message);
                new OneBotGroupMessage(e);
            }
            PluginPetService.LOGGER.info(message);
        } catch (Exception e){
            PluginPetService.LOGGER.warning(message, e);
        }
    }
}