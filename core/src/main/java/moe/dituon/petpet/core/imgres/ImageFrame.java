package moe.dituon.petpet.core.imgres;

import java.awt.image.BufferedImage;

public class ImageFrame {
    public final int delay;
    public final BufferedImage image;

    public ImageFrame(BufferedImage image, int delay) {
        this.delay = delay;
        this.image = image;
    }

    public ImageFrame(BufferedImage image) {
        this(image, 0);
    }
}
