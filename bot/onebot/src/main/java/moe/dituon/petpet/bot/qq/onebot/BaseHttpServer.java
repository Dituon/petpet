package moe.dituon.petpet.bot.qq.onebot;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;
import moe.dituon.petpet.core.utils.image.EncodedImage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BaseHttpServer {
    public final Cache<String, EncodedImage> cache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();
    public BaseHttpServer(int port) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 16);
        httpServer.createContext("/", new MyHandler());
        httpServer.setExecutor(new ThreadPoolExecutor(
                0, 16,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>())
        );
        log.info("HTTP 图像服务器已在端口 {} 上启动", port);
        httpServer.start();
    }


    class MyHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange ex) throws IOException {
            var id = ex.getRequestURI().getRawPath().substring(1);
            var image = cache.getIfPresent(id);
            if (image == null) {
                ex.sendResponseHeaders(404, -1);
                ex.close();
                return;
            }
            cache.invalidate(id);
            ex.sendResponseHeaders(200, image.bytes.length);
            OutputStream os = ex.getResponseBody();
            os.write(image.bytes);
            os.close();
            ex.close();
        }
    }
}
