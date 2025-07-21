package moe.dituon.petpet.core.filter;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

@Getter
@Setter
public class PetpetSwingTwistFilter {
    private final float maxAngle;
    private final float amplitudeFactor;
    private final float omega;
    private final float decay;

    public PetpetSwingTwistFilter() {
        this(0.5f, 0.5f, (float)(2.0 * Math.PI / 5.0), 0.1f);
    }

    /**
     * @param maxAngle 最大旋转角度[弧度]
     * @param amplitude 振幅系数, 相对图像宽度的比例
     * @param omega 波动角速度, 决定摆动速度
     * @param decay 衰减系数, 随时间递减幅度
     */
    public PetpetSwingTwistFilter(float maxAngle, float amplitude, float omega, float decay) {
        this.maxAngle = maxAngle;
        this.amplitudeFactor = amplitude;
        this.omega = omega;
        this.decay = decay;
    }

    public BufferedImage filter(BufferedImage input, int frameIndex) {
        int width = input.getWidth();

        float amplitude = width * amplitudeFactor;

        float t = frameIndex;
        float damping = (float) Math.exp(-decay * t);
        float offset = (float) (amplitude * Math.sin(omega * t) * damping);
        float angle = (float) (maxAngle * Math.sin(omega * t) * damping);

        return apply(input, offset, angle);
    }

    public BufferedImage apply(BufferedImage src, float globalOffset, float globalAngle) {
        int width = src.getWidth();
        int height = src.getHeight();

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        for (int y = 0; y < height; y++) {
            float factor = 1.0f - (float) y / height;
            float offset = globalOffset * factor;
            float angle = globalAngle * factor;

            AffineTransform transform = new AffineTransform();
            double cx = width / 2.0;
            double cy = height / 2.0;

            transform.translate(cx, cy);
            transform.rotate(angle);
            transform.translate(-cx, -cy);
            transform.translate(offset, 0);

            BufferedImage row = src.getSubimage(0, y, width, 1);
            g.setTransform(transform);
            g.drawImage(row, 0, y, null);
        }

        g.dispose();
        return result;
    }

}
