package moe.dituon.petpet.server;

import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
            ServerPetService.LOGGER.info("PetpetWebServer started in port " + service.port);

            httpServer.createContext("/petpet", new PetHttpHandler(service));
            httpServer.createContext("/preview", new PreviewHttpHandler(service));

            try(InputStream stream = this.getClass().getResourceAsStream("/index.html")) {
                if (stream == null) throw new Exception();
                InputStream html = new ByteArrayInputStream(stream.readAllBytes());
                httpServer.createContext("/", exchange -> PetHttpHandler.handleResponse(exchange, html, "text/html"));

                ServerPetService.LOGGER.info("WebUI-URL: http://127.0.0.1:" + service.port + '/');
                if (!service.usePreview) ServerPetService.LOGGER.warning("WebUI preview is disabled");
            } catch (Exception e){
                ServerPetService.LOGGER.info("Petpet WebUI is disabled");
            }

            httpServer.setExecutor(Executors.newFixedThreadPool(service.webServerThreadPoolSize));
            httpServer.start();

            apiUrl = ("http://127.0.0.1:" + service.port + "/petpet").intern();
            ServerPetService.LOGGER.info("API-URL: " + apiUrl);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getApiUrl(){
        return apiUrl;
    }
}
