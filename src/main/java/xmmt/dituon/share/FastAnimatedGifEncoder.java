package xmmt.dituon.share;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.NeuQuant;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;

public class FastAnimatedGifEncoder extends AnimatedGifEncoder {
    /**
     * 等效于父类的addFrame, 单线程处理BufferedImage(构建FrameData)
     * @deprecated 应当在多线程环境中预构建FrameData
     */
    @Deprecated
    @Override
    public boolean addFrame(BufferedImage image){
        FrameData frame = new FrameData(image, (byte) sample);
        addFrame(frame);
        return true;
    }

    public void addFrame(FrameData frame) {
        try {
            pixels = frame.pixels;
//            getImagePixels(); // convert to correct format if necessary
            analyzePixels(frame); // build color table & map pixels
            if (firstFrame) {
                writeLSD(); // logical screen descriptior
                writePalette(); // global color table
                if (repeat >= 0) {
                    // use NS app extension to indicate reps
                    writeNetscapeExt();
                }
            }
            writeGraphicCtrlExt(); // write graphic control extension
            writeImageDesc(); // image descriptor
            if (!firstFrame) {
                writePalette(); // local color table
            }
            writePixels(); // encode and write pixel data
            firstFrame = false;
        } catch (IOException ignored) {
        }
    }

    protected void analyzePixels(FrameData frame) {
        int len = pixels.length;
        int nPix = len / 3;
        indexedPixels = new byte[nPix];
        NeuQuant nq = frame.neuQuant;
        colorTab = frame.colorTab; // create reduced palette

        int p = 0;
        while (p < nPix) usedEntry[p++] = false;

        // map image pixels to new palette
        int k = 0;
        for (int i = 0; i < nPix; i++) {
            int index =
                    nq.map(pixels[k++] & 0xff,
                            pixels[k++] & 0xff,
                            pixels[k++] & 0xff);
            usedEntry[index] = true;
            indexedPixels[i] = (byte) index;
        }
        pixels = null;
        colorDepth = 8;
        palSize = 7;
        // get closest match to transparent color if specified
        if (transparent != null) {
            transIndex = transparentExactMatch ? findExact(transparent) : findClosest(transparent);
        }
    }

    public static class FrameData {
        public byte[] pixels;
        public byte[] colorTab;
        public NeuQuant neuQuant;

        FrameData(BufferedImage image, byte quality) {
            pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            neuQuant = new NeuQuant(pixels, pixels.length, quality < 1 ? 1 : quality);
            colorTab = neuQuant.process();
            // convert map from BGR to RGB
            for (int i = 0; i < colorTab.length; i += 3) {
                byte temp = colorTab[i];
                colorTab[i] = colorTab[i + 2];
                colorTab[i + 2] = temp;
//                usedEntry[i / 3] = false;
            }
        }
    }
}
