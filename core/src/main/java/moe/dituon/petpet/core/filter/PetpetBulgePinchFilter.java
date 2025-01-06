package moe.dituon.petpet.core.filter;

import lombok.Getter;
import lombok.Setter;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

@Getter
@Setter
public class PetpetBulgePinchFilter {

    protected float centerX = 0.5f;
    protected float centerY = 0.5f;
    protected float radius = 0;
    protected float strength = 0;

    public PetpetBulgePinchFilter(){}

    public BufferedImage filter(BufferedImage input) {
        if (strength == 0) return input;
        int cx = (int) (input.getWidth() * centerX);
        int cy = (int) (input.getHeight() * centerY);

        if (radius == 0) {
            radius = Math.min(cx, cy) / 2f;
        }

        int w = input.getWidth();
        int h = input.getHeight();
        BufferedImage outputImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        boolean isBulge = strength > 0;
        float radiusSquared = radius * radius;
        double inverseBulgeStrength = 1.0 / strength;

        WritableRaster inputRaster = input.getRaster();
        WritableRaster outputRaster = outputImage.getRaster();

        int[] pixel = {255, 255, 255, 255};

        for (int fx = 0; fx < w; fx++) {
            for (int fy = 0; fy < h; fy++) {
                int dx = fx - cx;
                int dy = fy - cy;
                double distanceSquared = dx * dx + dy * dy;
                int sx = fx;
                int sy = fy;

                if (distanceSquared < radiusSquared) {
                    if (isBulge) {
                        double distance = Math.sqrt(distanceSquared);
                        double dirX = dx / distance;
                        double dirY = dy / distance;
                        double alpha = 1 - distance / radius;
                        double distortionFactor = Math.pow(alpha, inverseBulgeStrength) * distance;

                        sx -= (int) (distortionFactor * dirX);
                        sy -= (int) (distortionFactor * dirY);
                    } else {
                        float d = (float) Math.sqrt(distanceSquared / radiusSquared);
                        float t = (float) Math.pow(Math.sin(Math.PI * 0.5 * d), strength);

                        dx *= t;
                        dy *= t;

                        sx = (cx + dx);
                        sy = (cy + dy);
                    }
                }
                if (sx >= 0 && sx < w && sy >= 0 && sy < h) {
                    inputRaster.getPixel(sx, sy, pixel);
                    outputRaster.setPixel(fx, fy, pixel);
                }
            }
        }
        return outputImage;
    }
}
