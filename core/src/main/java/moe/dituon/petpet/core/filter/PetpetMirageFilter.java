package moe.dituon.petpet.core.filter;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PetpetMirageFilter {
    @Getter
    @Setter
    protected float scaleI = 0.3f;
    @Getter
    @Setter
    protected float scaleC = 0.8f;
    @Getter
    @Setter
    protected float desatI = 0;
    @Getter
    @Setter
    protected float desatC = 0;
    @Getter
    @Setter
    protected float weightI = 0.7f;
    @Getter
    @Setter
    private boolean isColored = true;
    @Getter
    @Setter
    protected int maxSize = 1200;

    @Getter
    private BufferedImage innerImage;
    @Getter
    private BufferedImage coverImage;
    @Getter
    private BufferedImage outputImage;

    private int width, height;
    private int[] innerGray;
    private int[] coverGray;
    private float[] alphaCache;
    private int[] innerDataCache;
    private int[] coverDataCache;

    public PetpetMirageFilter() {}

    public void setInnerImage(BufferedImage img) {
        this.innerImage = img;

        // Calculate target dimensions
        if (maxSize > 0) {
            if (img.getWidth() > img.getHeight()) {
                width = maxSize;
                height = (int) Math.ceil((double) img.getHeight() * maxSize / img.getWidth());
            } else {
                height = maxSize;
                width = (int) Math.ceil((double) img.getWidth() * maxSize / img.getHeight());
            }
        } else {
            width = img.getWidth();
            height = img.getHeight();
        }

        // Resize image
        innerImage = resizeImage(img, width, height);
        innerGray = convertToGray(innerImage);
    }

    public void setCoverImage(BufferedImage img) {
        this.coverImage = img;
        coverImage = resizeImage(img, width, height);
        coverGray = convertToGray(coverImage);
    }

    private BufferedImage resizeImage(BufferedImage img, int width, int height) {
        Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    private int[] convertToGray(BufferedImage img) {
        int[] data = img.getRGB(0, 0, width, height, null, 0, width);
        int[] grayData = new int[data.length];

        for (int i = 0; i < data.length; i++) {
            int pixel = data[i];
            grayData[i] = (int) (
                    0.299f * ((pixel >> 16) & 0xFF)
                            + 0.587f * ((pixel >> 8) & 0xFF)
                            + 0.114f * (pixel & 0xFF)
            );
        }
        return grayData;
    }

    public void updateColorMode(boolean isColored) {
        this.isColored = isColored;
        process();
    }

    public BufferedImage filter(BufferedImage coverImage, BufferedImage innerImage) {
        boolean updateFlag = false;
        if (this.innerImage != innerImage) {
            this.setInnerImage(innerImage);
            updateFlag = true;
        }
        if (this.coverImage != coverImage) {
            this.setCoverImage(coverImage);
            updateFlag = true;
        }
        if (this.innerImage == null || this.coverImage == null) {
            throw new IllegalStateException("Inner or Cover image is null");
        }
        if (updateFlag) {
            process();
        }
        return outputImage;
    }

    public BufferedImage filter() {
        process();
        return outputImage;
    }

    protected void process() {
        if (innerImage == null || coverImage == null) {
            throw new IllegalStateException("Inner or Cover image is null");
        }

        int[] outputData;
        if (isColored) {
            boolean flag = false;

            if (innerDataCache == null) {
                int[] data = innerImage.getRGB(0, 0, width, height, null, 0, width);
                innerDataCache = new int[data.length];
                for (int i = 0; i < data.length; i++) {
                    int pixel = data[i];
                    int r = (int) (((pixel >> 16) & 0xFF) * scaleI);
                    int g = (int) (((pixel >> 8) & 0xFF) * scaleI);
                    int b = (int) ((pixel & 0xFF) * scaleI);
                    float l = innerGray[i] * scaleI;
                    r = (int) (r + (l - r) * desatI);
                    g = (int) (g + (l - g) * desatI);
                    b = (int) (b + (l - b) * desatI);
                    pixel = (r << 16) | (g << 8) | b;
                    innerDataCache[i] = pixel;
                }
                flag = true;
            }

            if (coverDataCache == null) {
                int[] data = coverImage.getRGB(0, 0, width, height, null, 0, width);
                coverDataCache = new int[data.length];
                for (int i = 0; i < data.length; i++) {
                    int pixel = data[i];
                    int r = 255 - (int) ((255 - ((pixel >> 16) & 0xFF)) * scaleC);
                    int g = 255 - (int) ((255 - ((pixel >> 8) & 0xFF)) * scaleC);
                    int b = 255 - (int) ((255 - (pixel & 0xFF)) * scaleC);
                    int l = 255 - (int) ((255 - coverGray[i]) * scaleC);
                    r = (int) (r + (l - r) * desatC);
                    g = (int) (g + (l - g) * desatC);
                    b = (int) (b + (l - b) * desatC);
                    coverDataCache[i] = (r << 16) | (g << 8) | b;
                }
                flag = true;
            }

            if (alphaCache == null || flag) {
                alphaCache = new float[innerGray.length];
                for (int i = 0; i < innerGray.length; i++) {
                    alphaCache[i] = Math.min(Math.max((255 + innerGray[i] * scaleI - (255 - (255 - coverGray[i]) * scaleC)) / 255, 0), 1);
                }
            }

            outputData = new int[innerGray.length];
            for (int i = 0; i < innerGray.length; i++) {
                float a = alphaCache[i];
                int ai = (int) (255 * a);
                int innerPixel = innerDataCache[i];
                int ir = (innerPixel >> 16) & 0xFF;
                int ig = (innerPixel >> 8) & 0xFF;
                int ib = innerPixel & 0xFF;
                int coverPixel = coverDataCache[i];
                int cr = (coverPixel >> 16) & 0xFF;
                int cg = (coverPixel >> 8) & 0xFF;
                int cb = coverPixel & 0xFF;
                int r = (int) (((ir - ai + 255 - cr) * weightI + ai - 255 + cr) / a);
                int g = (int) (((ig - ai + 255 - cg) * weightI + ai - 255 + cg) / a);
                int b = (int) (((ib - ai + 255 - cb) * weightI + ai - 255 + cb) / a);
                // limit to 0 to 255
                if (((r | g | b) & ~0xFF) != 0) {
                    r = (r & ~0xFF) != 0 ? (((~r) >> 31) & 0xFF) : r;
                    g = (g & ~0xFF) != 0 ? (((~g) >> 31) & 0xFF) : g;
                    b = (b & ~0xFF) != 0 ? (((~b) >> 31) & 0xFF) : b;
                }
                outputData[i] = (r << 16) | (g << 8) | b | (ai << 24);
            }
        } else {
            outputData = new int[width * height];
            for (int i = 0; i < innerGray.length; i++) {
                int li = (int) (innerGray[i] * scaleI);
                int lc = 255 - (int) ((255 - coverGray[i]) * scaleC);
                int a = 255 + li - lc;
                int l = li * 255 / a;

                outputData[i] = (l << 16) | (l << 8) | l | (a << 24);
            }
        }

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        output.setRGB(0, 0, width, height, outputData, 0, width);
        outputImage = output;
    }
}
