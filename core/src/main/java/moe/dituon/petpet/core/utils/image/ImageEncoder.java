package moe.dituon.petpet.core.utils.image;

import com.pngencoder.PngEncoder;
import moe.dituon.petpet.core.context.CanvasContext;
import moe.dituon.petpet.core.utils.io.FastByteArrayOutputStream;

public class ImageEncoder {
    protected static final int DEFAULT_BLOCK_SIZE = 8192;
    protected static final String DEFAULT_IMAGE_FORMAT = "png";
    protected static final String DEFAULT_ANIMATED_IMAGE_FORMAT = "gif";

    private ImageEncoder() {
    }

    /**
     * encode CanvasContext to png or gif
     */
    public static EncodedImage encodeImage(CanvasContext context) {
        if (context.getLength() == 1) {
            try (var out = new FastByteArrayOutputStream(DEFAULT_BLOCK_SIZE)) {
                new PngEncoder()
                        .withBufferedImage(context.getFrameList().get(0).image)
                        .toStream(out);
                byte[] bytes = out.toByteArrayUnsafe();
                return new EncodedImage(
                        bytes,
                        context.getWidth(), context.getHeight(),
                        DEFAULT_IMAGE_FORMAT
                );
            }
        }
        byte[] bytes = GifEncoder.encodeGif(context);
        return new EncodedImage(
                bytes,
                context.getWidth(), context.getHeight(),
                DEFAULT_ANIMATED_IMAGE_FORMAT
        );
    }
}
