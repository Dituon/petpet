package xmmt.dituon.server;

import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class WebServer {
    public static final ServerPetService petService = new ServerPetService();

    public static void main(String[] args) throws IOException {
        petService.readConfig();
        if (petService.headless) System.setProperty("java.awt.headless", "true");

        petService.readData(new File(petService.path));

        HttpServer httpServer = HttpServer.create(new InetSocketAddress(petService.port), 0);
        httpServer.createContext("/petpet", new PetHttpHandler());
        httpServer.setExecutor(Executors.newFixedThreadPool(petService.threadPoolSize));
        httpServer.start();

        System.out.println("PetpetWebServer started in port " + petService.port);
        System.out.println("API-URL: 127.0.0.1:" + petService.port + "/petpet");
    }
}
