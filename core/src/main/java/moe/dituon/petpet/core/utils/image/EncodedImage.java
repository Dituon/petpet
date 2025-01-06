package moe.dituon.petpet.core.utils.image;


import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class EncodedImage {
    public final byte[] bytes;
    public final int width;
    public final int height;
    public final String format;
    @Getter
    @Setter
    @Nullable
    protected File basePath = null;

    public EncodedImage(byte[] bytes, int width, int height, String format) {
        this.bytes = bytes;
        this.width = width;
        this.height = height;
        this.format = format;
    }

    public void save(String filePath) throws IOException {
        save(new File(filePath));
    }

    public void save(File file) throws IOException {
        if (basePath == null && !file.isAbsolute()) {
            throw new IllegalArgumentException("File path must be absolute or basePath must be set");
        }
        Path path = basePath == null ? file.toPath() : basePath.toPath().resolve(file.toPath());
        if (!Files.exists(path)) {
            path.getParent().toFile().mkdirs();
        }
        Files.write(path, bytes);
    }

    public static class EncodedAnimatedImage extends EncodedImage {
        public final int length;
        public final int[] delay;

        public EncodedAnimatedImage(byte[] bytes, int width, int height, String format, int length, int[] delay) {
            super(bytes, width, height, format);
            this.length = length;
            this.delay = delay;
        }
    }
}
