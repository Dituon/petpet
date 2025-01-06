package moe.dituon.petpet.core.imgres;

import lombok.Getter;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ImageFrameList implements List<ImageFrame> {
    @Delegate
    public final List<ImageFrame> frames;
    @Getter
    public final int imageType;
    @Getter
    public final int width;
    @Getter
    public final int height;

    public ImageFrameList(@NotNull final List<ImageFrame> frames) {
        this.frames = frames;
        this.imageType = frames.isEmpty() ? BufferedImage.TYPE_4BYTE_ABGR : frames.get(0).image.getType();
        this.width = frames.stream().mapToInt(f -> f.image.getWidth()).max().orElse(0);
        this.height = frames.stream().mapToInt(f -> f.image.getHeight()).max().orElse(0);
    }

    /**
     * @param delay range: 0 ~ 65535
     */
    public ImageFrameList(
            @NotNull final List<BufferedImage> images,
            @Range(from = 0, to = 65535) final int delay
    ) {
        this(images.stream()
                .map(img -> new ImageFrame(img, delay))
                .collect(Collectors.toList())
        );
    }

    public ImageFrameList(
            @NotNull final List<BufferedImage> images,
            final int @NotNull [] delay
    ) {
        this(buildFrames(images, delay));
    }

    private static List<ImageFrame> buildFrames(List<BufferedImage> images, int[] delay) {
        var list = new ArrayList<ImageFrame>(images.size());
        for (int i = 0; i < images.size(); i++) {
            list.add(new ImageFrame(images.get(i), delay[i % delay.length]));
        }
        return list;
    }

    public static ImageFrameList byImages(List<BufferedImage> images) {
        return new ImageFrameList(images, 0);
    }

    public static ImageFrameList byFps(List<BufferedImage> images, double fps) {
        return new ImageFrameList(images, (int) (1000 / fps));
    }

    public ImageFrameList(BufferedImage image) {
        this(List.of(new ImageFrame(image)));
    }

    public List<BufferedImage> getImages() {
        return frames.stream().map(f -> f.image).collect(Collectors.toList());
    }

    @Override
    public ImageFrame get(int index) {
        return frames.get(index % frames.size());
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return new HashSet<>(frames).containsAll(c);
    }
}
