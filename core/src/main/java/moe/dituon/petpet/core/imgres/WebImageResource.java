package moe.dituon.petpet.core.imgres;

import moe.dituon.petpet.core.GlobalContext;
import moe.dituon.petpet.core.utils.image.ImageDecoder;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class WebImageResource extends ImageResource {
    protected CompletableFuture<ImageFrameList> task = null;
    protected final URL url;
    protected final Function<URL, InputStream> openStreamFunction;

    public WebImageResource(URL url) {
        this.url = url;
        this.openStreamFunction = GlobalContext.getInstance().resourceManager::openStream;
    }

    public WebImageResource(URL url, Function<URL, InputStream> openStreamFunction) {
        this.url = url;
        this.openStreamFunction = openStreamFunction;
    }

    @Override
    public @Nullable String getSrc() {
        return url.toString();
    }

    @Override
    public CompletableFuture<ImageFrameList> getFrameListAsync() {
        if (this.task != null) return this.task;
        synchronized (this) {
            if (this.task != null) return this.task;
            this.task = CompletableFuture.supplyAsync(() -> {
                try {
                    return ImageDecoder.readImage(openStreamFunction.apply(url));
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }, GlobalContext.getInstance().resourceManager.executor);
            return this.task;
        }
    }

    @Override
    public CompletableFuture<ImageFrameList> getFrameListAsync(File basePath) {
        return this.getFrameListAsync();
    }

    public static WebImageResource getWebResourceUnsafe(URL url) {
        return new WebImageResource(url, u -> {
            try {
                return u.openStream();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to open stream for URL: " + url, e);
            }
        });
    }
}
