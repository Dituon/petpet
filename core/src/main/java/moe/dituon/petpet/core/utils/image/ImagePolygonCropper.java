package moe.dituon.petpet.core.utils.image;

import lombok.Getter;
import moe.dituon.petpet.core.length.LengthContext;
import moe.dituon.petpet.core.length.PercentageLength;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

public class ImagePolygonCropper implements ImageCropper {
    protected final Shape staticShape;
    protected final boolean isAbsolute;
    @Getter
    protected final List<List<PercentageLength>> points;

    public ImagePolygonCropper(List<List<PercentageLength>> points) {
        boolean isAbsolute = true;
        int[] pointsX = new int[points.size()];
        int[] pointsY = new int[points.size()];
        for (int i = 0; i < points.size(); i++) {
            var xy = points.get(i);
            if (xy.size() != 2) {
                throw new IllegalArgumentException("Points must be a list of pairs of Lengths");
            }
            boolean xyIsAbsolute = xy.get(0).isAbsolute() && xy.get(1).isAbsolute();
            isAbsolute = xyIsAbsolute;
            if (!xyIsAbsolute) break;
            pointsX[i] = (int) xy.get(0).getValue();
            pointsY[i] = (int) xy.get(1).getValue();
        }
        this.isAbsolute = isAbsolute;
        this.staticShape = isAbsolute ? new Polygon(pointsX, pointsY, points.size()) : null;
        this.points = points;
    }

    protected static BufferedImage crop(BufferedImage image, Shape shape) {
        var bounds = shape.getBounds();
        var output = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = output.createGraphics();
        g2d.setClip(shape);
        g2d.drawImage(image, -bounds.x, -bounds.y, null);
        return output;
    }

    @Override
    public BufferedImage crop(BufferedImage image) {
        if (this.isAbsolute) return crop(image, this.staticShape);
        return crop(image, 0, 0);
    }

    @Override
    public BufferedImage crop(BufferedImage image, int canvasWidth, int canvasHeight) {
        if (this.isAbsolute) return crop(image, this.staticShape);
        return this.crop(image, new LengthContext(canvasWidth, canvasHeight, image.getWidth(), image.getHeight()));
    }

    @Override
    public BufferedImage crop(BufferedImage image, LengthContext context) {
        if (this.isAbsolute) return crop(image, this.staticShape);
        int[] pointsX = new int[points.size()];
        int[] pointsY = new int[points.size()];
        for (int i = 0; i < points.size(); i++) {
            var xy = points.get(i);
            pointsX[i] = (int) xy.get(0).getValue(context, context.elementWidth);
            pointsY[i] = (int) xy.get(1).getValue(context, context.elementHeight);
        }
        var shape = new Polygon(pointsX, pointsY, points.size());
        return crop(image, shape);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImagePolygonCropper)) return false;
        ImagePolygonCropper that = (ImagePolygonCropper) o;
        return isAbsolute == that.isAbsolute && Objects.equals(points, that.points);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isAbsolute, points);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append('[');
        for (var point : points) {
            sb.append('[').append(point.get(0)).append(", ").append(point.get(1)).append("], ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append(']');
        return sb.toString();
    }

    public static ImageCropper create(List<List<PercentageLength>> points) {
        if (points.isEmpty()) return ImageCropper.empty();
        return new ImagePolygonCropper(points);
    }
}
