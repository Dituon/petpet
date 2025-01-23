package moe.dituon.petpet.core.utils.image;

import moe.dituon.petpet.core.length.Length;
import moe.dituon.petpet.core.length.LengthContext;
import moe.dituon.petpet.core.length.LengthType;
import moe.dituon.petpet.core.length.NumberLength;

import java.awt.image.BufferedImage;
import java.util.List;

public interface ImageCropper {
    BufferedImage crop(BufferedImage image);

    BufferedImage crop(BufferedImage image, int canvasWidth, int canvasHeight);

    BufferedImage crop(BufferedImage image, LengthContext context);

    static ImageCropper empty() {
        return EmptyImageCropper.INSTANCE;
    }

    class EmptyImageCropper extends ImageXYWHCropper {
        public static final EmptyImageCropper INSTANCE = new EmptyImageCropper();

        protected EmptyImageCropper() {
            super(new NumberLength(100, LengthType.CW), new NumberLength(100, LengthType.CH));
        }

        @Override
        public BufferedImage crop(BufferedImage image) {
            return image;
        }

        @Override
        public BufferedImage crop(BufferedImage image, LengthContext context) {
            return image;
        }
    }
}
