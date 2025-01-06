package moe.dituon.petpet.core.filter;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PetpetGrayFilter {
    protected PetpetGrayFilter() {
    }

    public static BufferedImage filter(BufferedImage image) {
        BufferedImage tempImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g2d = tempImage.getGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return tempImage;
    }
}
