package moe.dituon.petpet.httpserver;

import io.javalin.Javalin;
import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.imgres.BufferedImageResource;
import moe.dituon.petpet.core.imgres.ImageResourceMap;
import moe.dituon.petpet.core.utils.image.EncodedImage;
import moe.dituon.petpet.service.BaseService;
import moe.dituon.petpet.service.EnvironmentChecker;
import moe.dituon.petpet.service.TemplateUpdater;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@Slf4j
public class WebServer {
    public final ServerService service = new ServerService();

    public WebServer() {
        if (service.config.getHeadless()) {
            System.setProperty("java.awt.headless", "true");
        }
        var defaultFont = loadService();
        EnvironmentChecker.check();

        log.info("\u001B[95m\n\n" +
                "    ██████╗ ███████╗████████╗██████╗ ███████╗████████╗\n" +
                "    ██╔══██╗██╔════╝╚══██╔══╝██╔══██╗██╔════╝╚══██╔══╝\n" +
                "    ██████╔╝█████╗     ██║   ██████╔╝█████╗     ██║   \n" +
                "    ██╔═══╝ ██╔══╝     ██║   ██╔═══╝ ██╔══╝     ██║   \n" +
                "    ██║     ███████╗   ██║   ██║     ███████╗   ██║   \n" +
                "    ╚═╝     ╚══════╝   ╚═╝   ╚═╝     ╚══════╝   ╚═╝     " +
                "v" + BaseService.VERSION + "\n");
        log.info(String.format("已加载 %s 模板; 已注册 %s 脚本;", service.getStaticModelMap().size(), service.getScriptModelMap().size()));
        log.info(String.format("已加载 %s 字体; 默认字体为 %s;", GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames().length, defaultFont));

        Javalin.create(config -> config.showJavalinBanner = false)
                .get("/generate", ctx -> handleGenerate(ctx, null))
                .get("/generate/{id}", ctx -> handleGenerate(ctx, ctx.pathParam("id")))
                .post("/generate", ctx -> handlePostGenerate(ctx, null))
                .post("/generate/{id}", ctx -> handlePostGenerate(ctx, ctx.pathParam("id")))
                .get("/", ctx -> {
                    ctx.res().setContentType("text/plain; charset=utf-8");
                    ctx.result(service.getInfo());
                })
                .exception(Exception.class, (e, ctx) -> {
                    ctx.status(400);
                    ctx.res().setContentType("text/plain; charset=utf-8");
                    ctx.result(e.getMessage());
                })
                .start(service.config.getPort());
        log.info("Petpet Http Server 启动成功!");

        if (service.config.getUpdate().getEnabled()) new Thread(() -> {
            boolean success = new TemplateUpdater(service.config.getUpdate(), service).startUpdate();
            if (success) {
                log.info("Petpet 正在重载数据...");
                service.clear();
                var newDefaultFont = loadService();
                log.info(String.format("已加载 %s 模板; 已注册 %s 脚本;", service.getStaticModelMap().size(), service.getScriptModelMap().size()));
                log.info(String.format("已加载 %s 字体; 默认字体为 %s;", GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames().length, newDefaultFont));
            }
        }).start();
    }

    /**
     * @return 默认字体 name
     */
    protected String loadService() {
        for (String dataPath : service.config.getTemplatePath()) {
            service.addTemplates(new File(dataPath));
        }
        for (String fontPath : service.config.getFontPath()) {
            service.addFonts(new File(fontPath).toPath());
        }
        service.updateScriptService();
        return service.setDefaultFontFamily(service.config.getDefaultFontFamily());
    }

    protected void handleGenerate(Context ctx, String id) {
        var request = id == null ? PostRequest.fromQueryMap(ctx.queryParamMap()) : PostRequest.fromQueryMap(id, ctx.queryParamMap());
        var response = service.generate(request.getId(), request.toRequestContext());
        setResponse(ctx, response);
    }

    protected void handlePostGenerate(Context ctx, String id) {
        if (!ctx.isMultipartFormData()) {
            var request = id == null ?
                    PostRequest.fromString(ctx.body()) : PostRequest.fromString(id, ctx.body());
            var response = service.generate(request.getId(), request.toRequestContext());
            setResponse(ctx, response);
        } else {
            var textMap = extractTextMap(ctx);
            var imageMap = extractImageMap(ctx);
            var request = new RequestContext(imageMap, textMap);
            var response = service.generate(id, request);
            setResponse(ctx, response);
        }
    }

    protected HashMap<String, String> extractTextMap(Context ctx) {
        var textData = ctx.formParamMap();
        var textMap = new HashMap<String, String>(textData.size());
        for (var entry : textData.entrySet()) {
            textMap.put(entry.getKey(), entry.getValue().get(0));
        }
        return textMap;
    }

    protected ImageResourceMap extractImageMap(Context ctx) {
        var imageData = ctx.uploadedFileMap();
        var imageMap = new ImageResourceMap(imageData.size());
        for (var entry : imageData.entrySet()) {
            try {
                imageMap.put(entry.getKey(), new BufferedImageResource(entry.getValue().get(0).content()));
            } catch (IOException e) {
                throw new IllegalArgumentException("Can not read image '" + entry.getKey() + "'");
            }
        }
        return imageMap;
    }

    protected void setResponse(Context ctx, EncodedImage response) {
        ctx.contentType("image/" + response.format);
        ctx.result(response.bytes);
    }
}
