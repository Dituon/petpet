package moe.dituon.petpet.server;

import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class WebServer {
    public ServerPetService service = new ServerPetService();
    private String apiUrl;

    public WebServer() {
        service.readConfig();
        init();
    }

    public WebServer(ServerServiceConfig config) {
        service.readConfig(config);
        init();
    }

    private void init() {
        service.readData(new File(service.path).listFiles());

        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(service.port), 0);
            httpServer.createContext("/petpet", new PetHttpHandler(service));
            httpServer.createContext("/preview", new PreviewHttpHandler(service));
            httpServer.setExecutor(Executors.newFixedThreadPool(service.webServerThreadPoolSize));
            httpServer.start();

            System.out.println("PetpetWebServer started in port " + service.port);
            apiUrl = ("http://127.0.0.1:" + service.port + "/petpet").intern();
            System.out.println("API-URL: " + apiUrl);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getApiUrl(){
        return apiUrl;
    }
}
