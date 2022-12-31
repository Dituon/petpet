package moe.dituon.petpet.websocket.gocq;

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
        System.out.println("GoCQ API WebSocket 连接成功");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("GoCQ API WebSocket 连接关闭 (" + code + "): " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("an error occurred:" + ex);
    }

    @Override
    public void onMessage(String message) {
        try {
            GoCQGroupMessageEventDTO e = GoCQGroupMessageEventDTO.parse(message);
            System.out.println(e.toString());
            new OneBotGroupMessage(e);
        } catch (Exception e){
            System.err.println(message);
//            e.printStackTrace();
        }
    }
}