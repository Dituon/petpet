package moe.dituon.petpet.core.utils.image;

import com.madgag.gif.fmsware.GifDecoder;
import moe.dituon.petpet.core.imgres.ImageFrame;
import moe.dituon.petpet.core.imgres.ImageFrameList;

import javax.imageio.ImageIO;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class ImageDecoder {
    private ImageDecoder() {
    }

    /**
     * If stream is a Gif, it will return all frames
     */
    public static ImageFrameList readImage(InputStream inputStream) throws IOException {
        if (!inputStream.markSupported()) {
            inputStream = new BufferedInputStream(inputStream);
        }

        if (!checkIsGif(inputStream)) {
            // not gif
            return new ImageFrameList(ImageIO.read(inputStream));
        }

        // read gif
        GifDecoder decoder = new ReusableGifDecoder();
        decoder.read(inputStream);
        List<ImageFrame> output = new ArrayList<>(decoder.getFrameCount());
        for (var i = 0; i < decoder.getFrameCount(); i++) {
            output.add(new ImageFrame(decoder.getFrame(i), decoder.getDelay(i)));
        }
        return new ImageFrameList(output);
    }

    public static boolean checkIsGif(InputStream inputStream) throws IOException {
        inputStream.mark(0);
        byte[] bytes = inputStream.readNBytes(3);
        inputStream.reset();
        return bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F';
    }
}
