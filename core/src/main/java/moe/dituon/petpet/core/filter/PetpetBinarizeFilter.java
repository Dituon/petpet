package moe.dituon.petpet.core.filter;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PetpetBinarizeFilter {
    protected PetpetBinarizeFilter() {}

    public static BufferedImage filter(BufferedImage image) {
        int h = image.getHeight();
        int w = image.getWidth();
        BufferedImage binarizedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int val = image.getRGB(i, j);
                int r = (0x00ff0000 & val) >> 16;
                int g = (0x0000ff00 & val) >> 8;
                int b = (0x000000ff & val);
                int m = (r + g + b);
                if (m >= 383) {
                    binarizedImage.setRGB(i, j, Color.WHITE.getRGB());
                } else {
                    binarizedImage.setRGB(i, j, 0);
                }
            }
        }
        return binarizedImage;
    }
}
