package moe.dituon.petpet.core.utils.image;

import moe.dituon.petpet.core.length.Length;
import moe.dituon.petpet.core.length.LengthContext;
import moe.dituon.petpet.core.length.LengthType;
import moe.dituon.petpet.core.length.NumberLength;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

public class ImageXYWHCropper implements ImageCropper {
    public final Length x1;
    public final Length y1;
    public final Length x2;
    public final Length y2;
    public final boolean isAbsolute;
    public final boolean isEmpty;
    protected int staticX1 = 0;
    protected int staticY1 = 0;
    protected int staticX2 = 0;
    protected int staticY2 = 0;

    public ImageXYWHCropper(
            @Nullable Length x1, @Nullable Length y1,
            @NotNull Length x2, @NotNull Length y2
    ) {
        if (x1 == null) x1 = NumberLength.EMPTY;
        if (y1 == null) y1 = NumberLength.EMPTY;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.isAbsolute = x1.isAbsolute() && y1.isAbsolute() && x2.isAbsolute() && y2.isAbsolute();
        if (isAbsolute) {
            this.isEmpty = x1.getRawValue() == 0 && y1.getRawValue() == 0 && x2.getRawValue() == 0 && y2.getRawValue() == 0;

            this.staticX1 = Math.round(x1.getValue());
            this.staticY1 = Math.round(y1.getValue());
            this.staticX2 = Math.round(x2.getValue());
            this.staticY2 = Math.round(y2.getValue());
        } else {
            this.isEmpty = false;
        }
    }

    public ImageXYWHCropper(Length width, Length height) {
        this(NumberLength.EMPTY, NumberLength.EMPTY, width, height);
    }

    @Override
    public BufferedImage crop(BufferedImage image) {
        if (this.isAbsolute) return image.getSubimage(staticX1, staticY1, staticX2 - staticX1, staticY2 - staticY1);
        return this.crop(image, 0, 0);
    }

    @Override
    public BufferedImage crop(BufferedImage image, int canvasWidth, int canvasHeight) {
        return this.crop(image, new LengthContext(canvasWidth, canvasHeight, image.getWidth(), image.getHeight()));
    }

    @Override
    public BufferedImage crop(BufferedImage image, LengthContext context) {
        if (this.isAbsolute) return this.crop(image);

        int tx1 = Math.round(this.x1.getValue(context));
        int ty1 = Math.round(this.y1.getValue(context));
        int tx2 = Math.round(this.x2.getValue(context));
        int ty2 = Math.round(this.y2.getValue(context));
        return image.getSubimage(tx1, ty1, tx2 - tx1, ty2 - ty1);
    }

    public static ImageXYWHCropper create(List<Length> cropCoords) {
        switch (cropCoords.size()) {
            case 0:
                return ImageXYWHCropper.EmptyImageCropper.INSTANCE;
            case 1:
                return new ImageXYWHCropper(cropCoords.get(0), cropCoords.get(0));
            case 2:
                return new ImageXYWHCropper(cropCoords.get(0), cropCoords.get(1));
            case 4:
                return new ImageXYWHCropper(cropCoords.get(0), cropCoords.get(1), cropCoords.get(2), cropCoords.get(3));
            default:
                throw new IllegalArgumentException("Invalid crop coordinates");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageXYWHCropper)) return false;
        ImageXYWHCropper that = (ImageXYWHCropper) o;
        return Objects.equals(x1, that.x1) && Objects.equals(y1, that.y1) && Objects.equals(x2, that.x2) && Objects.equals(y2, that.y2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x1, y1, x2, y2);
    }

    @Override
    public String toString() {
        return String.format("[%s, %s, %s, %s]", x1, y1, x2, y2);
    }
}
