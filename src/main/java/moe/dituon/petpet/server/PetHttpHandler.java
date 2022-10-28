package moe.dituon.petpet.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class PetHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) {
        try {
            RequestParser parser;
            if (httpExchange.getRequestMethod().equals("GET")) { //GET

                String requestParam = httpExchange.getRequestURI().getRawQuery();
                if (requestParam == null) {
                    handleResponse(httpExchange, WebServer.petService.getIndexJson());
                    return;
                }

                parser = new GETParser(requestParam);
            } else {
                //POST
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(httpExchange.getRequestBody(), StandardCharsets.UTF_8)
                );

                StringBuilder requestBodyContent = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    requestBodyContent.append(line);
                }

                parser = new POSTParser(requestBodyContent.toString());
            }

            handleResponse(httpExchange, parser.getImagePair().getFirst(), parser.getImagePair().getSecond());
            parser.close();

        } catch (Exception e) {
            e.printStackTrace();
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

