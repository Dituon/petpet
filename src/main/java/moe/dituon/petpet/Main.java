package moe.dituon.petpet;

import moe.dituon.petpet.server.WebServer;
import moe.dituon.petpet.share.BaseLogger;
import moe.dituon.petpet.share.BasePetService;
import moe.dituon.petpet.websocket.gocq.GoCQPetpet;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");
        BaseLogger.getInstance().info("\u001B[95m\n\n" +
                "                ▄▀▄     ▄▀▄\n" +
                "               ▄█░░▀▀▀▀▀░░█▄\n" +
                "           ▄▄  █░░░░░░░░░░░█  ▄▄\n" +
                "          █▄▄█ █░░▀░░┬░░▀░░█ █▄▄█\n" +
                "█▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀█\n" +
                "█                _                 _     █\n" +
                "█   _ __    ___ | |_  _ __    ___ | |_   █\n" +
                "█  | '_ \\  / _ \\| __|| '_ \\  / _ \\| __|  █\n" +
                "█  | |_) ||  __/| |_ | |_) ||  __/| |_   █\n" +
                "█  | .__/  \\___| \\__|| .__/  \\___| \\__|  █\n" +
                "█  |_|               |_|                 █\n" +
                "█                                        █\n" +
                "█▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄█    v" +
                BasePetService.VERSION + "\n");
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
