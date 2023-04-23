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
                "    ooooooooo.                 .                            .   \n" +
                "    `888   `Y88.             .o8                          .o8   \n" +
                "     888   .d88'  .ooooo.  .o888oo oo.ooooo.   .ooooo.  .o888oo \n" +
                "     888ooo88P'  d88' `88b   888    888' `88b d88' `88b   888   \n" +
                "     888         888ooo888   888    888   888 888ooo888   888   \n" +
                "     888         888    .o   888 .  888   888 888    .o   888 . \n" +
                "    o888o        `Y8bod8P'   \"888\"  888bod8P' `Y8bod8P'   \"888\" \n" +
                "                                    888                         \n" +
                "                                   o888o                        \n" +
                "                                                                     v" +
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
