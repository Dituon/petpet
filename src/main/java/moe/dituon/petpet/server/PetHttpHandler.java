package moe.dituon.petpet.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class PetHttpHandler implements HttpHandler {
    private final ServerPetService service;
    private final byte[] indexJsonBytes;

    PetHttpHandler(ServerPetService service) {
        super();
        this.service = service;
        this.indexJsonBytes = service.getIndexJson().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void handle(HttpExchange httpExchange) {
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        try {
            RequestParser parser;
            if (httpExchange.getRequestMethod().equals("GET")) { //GET

                String requestParam = httpExchange.getRequestURI().getRawQuery();
                if (requestParam == null) {
                    handleResponseIndex(httpExchange);
                    return;
                }

                ServerPetService.LOGGER.info("DEBUG[GET QUERY]: " + requestParam);
                parser = new GETParser(service, requestParam);
                finish(httpExchange, parser);
                return;
            }

            // POST
            Headers headers = httpExchange.getRequestHeaders();
            String contentType = headers.getFirst("Content-Type");

            if (contentType.startsWith("multipart/form-data")) {
                parser = new FormDataParser(service, httpExchange);

                finish(httpExchange, parser);
                return;
            }

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(httpExchange.getRequestBody(), StandardCharsets.UTF_8)
            );

            StringBuilder requestBodyContent = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                requestBodyContent.append(line);
            }

            String content = requestBodyContent.toString();
            ServerPetService.LOGGER.info(requestBodyContent.insert(0, "DEBUG[POST BODY]: ").toString());
            parser = new POSTParser(service, content);
            finish(httpExchange, parser);

        } catch (Exception ex) {
            ex.printStackTrace();
            handleResponse(httpExchange, 400, ex.toString());
        }
    }

    static private void handleResponse(HttpExchange httpExchange, int rCode, String str) {
        try {
            httpExchange.sendResponseHeaders(rCode, 0);
            OutputStream out = httpExchange.getResponseBody();
            out.write(str.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    static public void handleResponse(HttpExchange httpExchange, InputStream input, String mime) throws IOException {
        input.reset();
        httpExchange.getResponseHeaders().add("Content-Type", mime);
        httpExchange.sendResponseHeaders(200, input.available());
        OutputStream out = httpExchange.getResponseBody();
        input.transferTo(out);
        out.flush();
        out.close();
    }

    private void handleResponseIndex(HttpExchange httpExchange) throws IOException {
        httpExchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        httpExchange.sendResponseHeaders(200, indexJsonBytes.length);

        OutputStream out = httpExchange.getResponseBody();
        out.write(indexJsonBytes);
        out.flush();
        out.close();
    }

    private void finish(HttpExchange httpExchange, RequestParser parser) throws IOException {
        handleResponse(httpExchange, parser.getImagePair().getFirst(), ("image/" + parser.getImagePair().getSecond()).intern());
        parser.close();
    }
}

