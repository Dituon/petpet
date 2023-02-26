package moe.dituon.petpet;

import moe.dituon.petpet.server.WebServer;
import moe.dituon.petpet.websocket.gocq.GoCQPetpet;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");
        if (args.length == 0) {
            WebServer server = new WebServer();
            return;
        }
        List<String> param = Arrays.asList(args);
        if (param.contains("-gocq")) {
            try {
                GoCQPetpet goCQPetpet = GoCQPetpet.getInstance();
            } catch (NoClassDefFoundError e) {
                e.printStackTrace();
                System.err.println("无法加载WebSocket依赖, 请使用分发版本");
            }
        }
    }
}
