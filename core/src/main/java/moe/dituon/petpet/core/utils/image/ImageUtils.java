package moe.dituon.petpet.core.utils.image;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

public class ImageUtils {
    public static BufferedImage cloneImage(BufferedImage raw) {
        ColorModel cm = raw.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = raw.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public static BufferedImage cloneImageAsAbgr(BufferedImage raw) {
        BufferedImage image = new BufferedImage(raw.getWidth(), raw.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        var g2d = image.createGraphics();
        g2d.drawImage(raw, 0, 0, null);
        g2d.dispose();
        return image;
    }
}
