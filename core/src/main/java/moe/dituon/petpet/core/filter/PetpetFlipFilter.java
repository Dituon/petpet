package moe.dituon.petpet.core.filter;

import java.awt.image.BufferedImage;

public class PetpetFlipFilter {
    protected PetpetFlipFilter() {
    }

    public static BufferedImage filter(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        var img = new BufferedImage(w, h, image.getType());
        var g2d = img.createGraphics();
        g2d.drawImage(image, 0, h, w, -h, null);
        return img;
    }
}
