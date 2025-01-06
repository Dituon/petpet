package moe.dituon.petpet.core.imgres;

import moe.dituon.petpet.core.utils.image.ImageDecoder;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class BufferedImageResource extends ImageResource {
    public final ImageFrameList frameList;

    public BufferedImageResource(ImageFrameList frameList) {
        this.frameList = frameList;
    }

    public BufferedImageResource(BufferedImage image) {
        this(new ImageFrameList(image));
    }

    public BufferedImageResource(InputStream stream) throws IOException {
        this(ImageDecoder.readImage(stream));
    }

    @Override
    public CompletableFuture<ImageFrameList> getFrameListAsync() {
        return CompletableFuture.completedFuture(this.frameList);
    }

    @Override
    public CompletableFuture<ImageFrameList> getFrameListAsync(File basePath) {
        return getFrameListAsync();
    }

    @Override
    public @Nullable String getSrc() {
        return null;
    }
}
