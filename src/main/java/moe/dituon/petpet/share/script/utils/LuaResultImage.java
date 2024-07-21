package moe.dituon.petpet.share.script.utils;

import moe.dituon.petpet.share.template.ResultImage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LuaResultImage {
    public final int width;
    public final int height;
    public final String mime;
    public final String suffix;
    protected final byte[] blob;
    protected final Path basePath;

    public LuaResultImage(ResultImage image, Path basePath) {
        this.basePath = basePath;
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.blob = image.getBlob();
        this.mime = image.getMime();
        this.suffix = image.getSuffix();
        System.out.println(blob.length);
    }

    public void saveAs(String path) throws IOException {
        var targetPath = basePath.resolve(path);
        Files.write(targetPath, blob);
    }
}
