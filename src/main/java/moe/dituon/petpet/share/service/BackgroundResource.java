package moe.dituon.petpet.share.service;

import lombok.Getter;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Random;

@Getter
public class BackgroundResource extends ImageResource {
    private static final Random random = new Random();
    private static final Object[] empty = new Object[0];

    protected final File[] files;
    protected final String name;
    @Setter
    protected boolean randomFlag;
    protected final Path basePath;

    public BackgroundResource() {
        files = (File[]) empty;
        name = null;
        basePath = null;
        randomFlag = true;
    }

    public BackgroundResource(File templateRoot) {
        this(templateRoot, false);
    }

    /**
     * @param templateRoot 背景资源路径
     * @param randomFlag   随机选取一个图像
     */
    public BackgroundResource(File templateRoot, boolean randomFlag) {
        if (!templateRoot.isDirectory()) throw new RuntimeException("BackgroundResource must be a directory");
        this.basePath = templateRoot.toPath();
        this.name = templateRoot.getName();
        this.files = Arrays.stream(Objects.requireNonNull(templateRoot.listFiles()))
                .filter(file -> file.isFile() && ImageResource.indexedImagePattern.matcher(file.getName()).matches())
                .sorted(Comparator.comparingInt(f -> {
                    String fileName = f.getName();
                    int endIndex = fileName.lastIndexOf(".");
                    return Integer.parseInt(fileName.substring(0, endIndex));
                }))
                .toArray(File[]::new);
        this.randomFlag = randomFlag;
    }

    public BufferedImage[] getImages() throws IOException {
        if (randomFlag) {
            if (files.length == 0) return (BufferedImage[]) empty;
            return new BufferedImage[]{
                    ImageIO.read(files[random.nextInt(files.length)])
            };
        }
        if (imagesRef.get() != null) {
            return imagesRef.get();
        }
        var images = new BufferedImage[files.length];
        for (int i = 0; i < files.length; i++) {
            images[i] = ImageIO.read(files[i]);
        }
        imagesRef = new WeakReference<>(images);
        return images;
    }
}
