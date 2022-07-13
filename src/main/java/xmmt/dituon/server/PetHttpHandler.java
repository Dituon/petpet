package xmmt.dituon.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 处理/myserver路径请求的处理器类
 */
public class PetHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) {
        try {
            String query = httpExchange.getRequestURI().getQuery();
            if (query == null) {
                StringBuilder json = new StringBuilder("{\"petData\": [");
                WebServer.petService.getDataMap().forEach((key, data) -> {
                    json.append("{\"key\": \"").append(key).append("\", ").append("\"types\": [");
                    data.getAvatar().forEach(avatar -> { //necessary data
                        json.append("\"").append(avatar.getType()).append("\", ");
                    });
                    if (!data.getAvatar().isEmpty()) json.delete(json.length() - 2, json.length());
                    json.append("]}, ");
                });
                if (!WebServer.petService.getDataMap().isEmpty()) json.delete(json.length() - 2, json.length());
                json.append("]}");
                handleResponse(httpExchange, json.toString());
                return;
            }

            CommandParser parser = new CommandParser(query);
            handleResponse(httpExchange, parser.getImagePair().getFirst(), parser.getImagePair().getSecond());
            parser.close();
        } catch (Exception ignored) {
            handleResponse(httpExchange, 400);
        }
    }

    private void handleResponse(HttpExchange httpExchange, int rCode) {
        try {
            httpExchange.sendResponseHeaders(rCode, 0);
            OutputStream out = httpExchange.getResponseBody();
            out.flush();
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleResponse(HttpExchange httpExchange, InputStream input, String type) throws IOException {
        byte[] imageBytes = input.readAllBytes();

        httpExchange.getResponseHeaders().add("Content-Type:", "image/" + type);
        httpExchange.sendResponseHeaders(200, imageBytes.length);
        OutputStream out = httpExchange.getResponseBody();
        out.write(imageBytes);
        out.flush();
        out.close();
    }

    private void handleResponse(HttpExchange httpExchange, String responseJson) throws Exception {
        byte[] responseContentByte = responseJson.getBytes(StandardCharsets.UTF_8);
        httpExchange.getResponseHeaders().add("Content-Type:", "application/json;charset=utf-8");
        httpExchange.sendResponseHeaders(200, responseContentByte.length);

        OutputStream out = httpExchange.getResponseBody();
        out.write(responseContentByte);
        out.flush();
        out.close();
    }
}

