package moe.dituon.petpet.websocket.gocq;

import moe.dituon.petpet.plugin.PluginPetService;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class GoCQAPIWebSocketClient extends WebSocketClient {
    public GoCQRequester requester = new GoCQRequester(this);

    public GoCQAPIWebSocketClient(URI serverURI) {
        super(serverURI);
        connect();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        PluginPetService.LOGGER.info("GoCQ Event WebSocket 连接成功");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        PluginPetService.LOGGER.info("GoCQ Event WebSocket 连接关闭 (" + code + "): " + reason);
    }

    @Override
    public void onError(Exception ex) {
        PluginPetService.LOGGER.warning("an error occurred:" + ex);
    }

    @Override
    public void onMessage(String message) {
        try {
            GoCQResponseDTO response = GoCQResponseDTO.parse(message);
            Long id = Long.parseLong(response.getEcho());
            ThreadLockObject<GoCQMemberDTO> lock = requester.getThreadLock(id);
            synchronized (lock) {
                lock.set(response.toGetGroupMemberResponseDTO().getData());
                lock.notify();
            }
        } catch (Exception ignored) {
        }
    }

}