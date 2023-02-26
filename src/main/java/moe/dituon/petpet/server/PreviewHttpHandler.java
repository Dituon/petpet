package moe.dituon.petpet.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kotlin.Pair;
import moe.dituon.petpet.share.BaseConfigFactory;
import moe.dituon.petpet.share.TextExtraData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class PreviewHttpHandler implements HttpHandler {
    static final String PREVIEW_ROOT = "./preview";
    static final String PREVIEW_CONFIG_PATH = "./preview-config.json";

    private final ServerPetService service;
    //    private HashMap<File, String> mimeMap = null;
    private String mime = "image/png";

    PreviewHttpHandler(ServerPetService service) {
        this.service = service;
        if (!service.usePreview) return;
        try {
            File configFile = new File(PREVIEW_CONFIG_PATH);
            if (!configFile.exists()) {
                Files.write(configFile.toPath(), new PreviewConfigDTO().stringify().getBytes());
            }
            PreviewConfigDTO config = PreviewConfigDTO.decodeFromString(service.getFileStr(configFile));

//            mimeMap = new HashMap<>(service.getDataMap().size());
            var avatars = BaseConfigFactory.cacheAvatarExtraDataProvider(
                    BaseConfigFactory.getGifAvatarExtraDataFromUrls(
                            config.getForm().getAvatar(),
                            config.getTo().getAvatar(),
                            config.getGroup().getAvatar(),
                            config.getBot().getAvatar(),
                            config.getRandomAvatarList()
                    )
            );
            var texts = new TextExtraData(
                    config.getForm().getName(),
                    config.getTo().getName(),
                    config.getGroup().getName(),
                    config.getTextList()
            );
            var time = System.currentTimeMillis();
            service.getDataMap().forEach((k, v) -> {
                String basePath = PREVIEW_ROOT + File.separatorChar + k + '.';
                if (
                        new File(basePath + "png").exists() || new File(basePath + "gif").exists()
                ) return; //skip if exists

                var stime = System.currentTimeMillis();
                Pair<InputStream, String> result = service.generateImage(
                        k, avatars, texts, null
                );
                var snowTime = System.currentTimeMillis();
                System.out.println(k + ' ' + (snowTime - stime) + "ms");
                File targetFile = new File(basePath + result.getSecond());
                try {
                    Files.copy(result.getFirst(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            var nowTime = System.currentTimeMillis();
            System.out.println("generate " + service.getDataMap().size() + " image in " + (nowTime - time) + "ms");
            mime = "image/gif";
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String root = "./";
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        File file = new File(root + path).getCanonicalFile();
        OutputStream os = exchange.getResponseBody();

        if (!file.exists()) {
            exchange.sendResponseHeaders(404, 0);
            os.flush();
            os.close();
            return;
        }

        Headers h = exchange.getResponseHeaders();
        h.set("Content-Type", mime);
        exchange.sendResponseHeaders(200, file.length());

        Files.copy(file.toPath(), os);
        os.close();
    }
}