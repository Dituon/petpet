package moe.dituon.petpet.core.utils.image;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.NeuQuant;
import moe.dituon.petpet.core.GlobalContext;
import moe.dituon.petpet.core.context.CanvasContext;
import moe.dituon.petpet.core.utils.stream.FastByteArrayOutputStream;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class GifEncoder {
    protected static final int DEFAULT_BLOCK_SIZE = 8192;

    protected GifEncoder() {
    }

    public static byte[] encodeGif(CanvasContext context) {
        try {
            CountDownLatch latch = new CountDownLatch(context.getLength());
            Encoder.FrameData[] frames = new Encoder.FrameData[context.getLength()];
            for (int i = 0; i < context.getLength(); i++) {
                int fi = i;
                GlobalContext.getInstance().imageProcessExecutor.execute(() -> {
                    // TODO: no copy
                    BufferedImage temp = new BufferedImage(context.getWidth(), context.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
                    Graphics2D g = temp.createGraphics();
                    g.drawImage(context.getFrameList().get(fi).image, 0, 0, null);
                    g.dispose();
                    // TODO: params.getQuality()
                    Encoder.FrameData frameData = new Encoder.FrameData(temp, 10);
                    frames[fi] = frameData;
                    latch.countDown();
                });
            }

            Encoder gifEncoder = new Encoder();
            FastByteArrayOutputStream output = new FastByteArrayOutputStream(DEFAULT_BLOCK_SIZE);
            gifEncoder.start(output);
            gifEncoder.setRepeat(0);
            gifEncoder.setQuality(context.getRenderConfig().getGifQuality());

            latch.await();
            gifEncoder.setSize(context.getWidth(), context.getHeight());
// TODO:           if (params.getReverse()) {
            for (int i = 0; i < frames.length; i++) {
                gifEncoder.setDelay(context.getFrameList().get(i).delay);
                gifEncoder.addFrame(frames[i]);
            }
            gifEncoder.finish();
            output.close();
            return output.toByteArrayUnsafe();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    public static class Encoder extends AnimatedGifEncoder {
        /**
         * 等效于父类的 addFrame, 单线程处理 BufferedImage(构建FrameData) <br/>
         * 应当在多线程环境中预构建FrameData
         */
        @Override
        public boolean addFrame(BufferedImage image) {
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
                // ignore errors
            }
        }

        protected void analyzePixels(FrameData frame) {
            colorTab = frame.colorTab; // create reduced palette
            usedEntry = frame.usedEntry;
            indexedPixels = frame.indexedPixels;

            pixels = null;
            colorDepth = 8;
            palSize = 7;
            // get closest match to transparent color if specified
            if (transparent != null) {
                transIndex = transparentExactMatch ? findExact(transparent) : findClosest(transparent);
            }
        }

        public static class FrameData {
            public final byte[] pixels;
            public final byte[] colorTab;
            public final NeuQuant neuQuant;
            byte[] indexedPixels;
            boolean[] usedEntry = new boolean[256];

            public FrameData(BufferedImage image, int quality) {
                pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                neuQuant = new NeuQuant(pixels, pixels.length, Math.max(quality, 1));
                colorTab = neuQuant.process();
                // convert map from BGR to RGB
                for (int i = 0; i < colorTab.length; i += 3) {
                    byte temp = colorTab[i];
                    colorTab[i] = colorTab[i + 2];
                    colorTab[i + 2] = temp;
                    usedEntry[i / 3] = false;
                }

                int nPix = pixels.length / 3;
                indexedPixels = new byte[nPix];

                int k = 0;
                for (int i = 0; i < nPix; i++) {
                    int index =
                            neuQuant.map(pixels[k++] & 0xff,
                                    pixels[k++] & 0xff,
                                    pixels[k++] & 0xff);
                    usedEntry[index] = true;
                    indexedPixels[i] = (byte) index;
                }
            }
        }
    }
}
