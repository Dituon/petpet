package xmmt.dituon.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {
    //TODO 作为网络服务器生成图片，可被其它语言使用
    public static short port = 2333;
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(port);
        while (true){
            Socket socket = server.accept();
            new Thread(() -> {
                try {
                    InputStream input = socket.getInputStream();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
    }
}
