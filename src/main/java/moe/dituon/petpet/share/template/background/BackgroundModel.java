package moe.dituon.petpet.share.template.background;

import moe.dituon.petpet.share.BackgroundData;
import moe.dituon.petpet.share.service.BackgroundResource;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class BackgroundModel {
    protected BackgroundData data;
    protected BackgroundResource resource;
    protected int width;
    protected int height;

    public BackgroundModel(BackgroundResource resource, BackgroundData data, int width, int height) {
        this.resource = resource;
        this.data = data;
        this.width = width;
        this.height = height;
    }

    public BufferedImage[] getImages() throws IOException {
        if (this.data == null) {
            return this.resource.getImages();
        }

        var backgrounds = this.resource.getImages();

        var length = this.data.getLength();
        BufferedImage[] arr = new BufferedImage[length];
        for (int i = 0; i < length; i++) {
            BufferedImage output = new BufferedImage(width, height, 1);
            Graphics2D g2d = output.createGraphics();
            g2d.setColor(data.getAwtColor());
            g2d.fillRect(0, 0, width, height);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0F));

            if (backgrounds.length != 0) {
                var bg = backgrounds[i % backgrounds.length];
                //TODO: 背景排版
                g2d.drawImage(bg, 0, 0, null);
            }
            arr[i] = output;
        }
        return arr;
    }
}
