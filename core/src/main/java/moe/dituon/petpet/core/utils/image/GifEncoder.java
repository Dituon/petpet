package moe.dituon.petpet.core.utils.image;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.NeuQuant;
import moe.dituon.petpet.core.GlobalContext;
import moe.dituon.petpet.core.context.CanvasContext;
import moe.dituon.petpet.core.utils.stream.FastByteArrayOutputStream;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class GifEncoder {
    protected static final int DEFAULT_BLOCK_SIZE = 8192;

    protected GifEncoder() {
    }

    public static byte[] encodeGif(CanvasContext context) {
        try {
            CountDownLatch latch = new CountDownLatch(context.getLength());
            int quality = context.getRenderConfig().getGifQuality();
            Encoder.FrameData[] frames = new Encoder.FrameData[context.getLength()];
            for (int i = 0; i < context.getLength(); i++) {
                int fi = i;
                GlobalContext.getInstance().imageProcessExecutor.execute(() -> {
                    var img = context.getFrameList().get(fi).image;
                    Encoder.FrameData frameData;
                    if (img.getType() == BufferedImage.TYPE_4BYTE_ABGR
                            || img.getType() == BufferedImage.TYPE_4BYTE_ABGR_PRE) {
                        frameData = Encoder.FrameData.fromAbgr(img, quality);
                    } else if (img.getType() == BufferedImage.TYPE_3BYTE_BGR) {
                        frameData = Encoder.FrameData.fromBgr(img, quality);
                    } else {
                        frameData = Encoder.FrameData.fromAbgr(to4ByteAbgr(img), quality);
                    }
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
// TODO:           params.getReverse()
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

    protected static BufferedImage to4ByteAbgr(BufferedImage image) {
        var result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        var g2d = result.getGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return result;
    }

    public static class Encoder extends AnimatedGifEncoder {
        /**
         * 等效于父类的 addFrame, 单线程处理 BufferedImage(构建FrameData) <br/>
         * 应当在多线程环境中预构建FrameData
         */
        @Override
        public boolean addFrame(BufferedImage image) {
            FrameData frame = FrameData.fromAbgr(image, (byte) sample);
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
            transIndex = frame.transIndex;
            if (transIndex != -1) {
                transparent = Color.BLACK;
            }

            pixels = null;
            colorDepth = 8;
            palSize = 7;
        }

        public static abstract class FrameData {
            public byte[] pixels;
            public byte[] colorTab;
            byte[] indexedPixels;
            boolean[] usedEntry = new boolean[256];
            int transIndex = -1;

            public static FrameDataBGR fromBgr(BufferedImage image, int quality) {
                return new FrameDataBGR(image, quality);
            }

            public static FrameDataABGR fromAbgr(BufferedImage image, int quality) {
                return new FrameDataABGR(image, quality);
            }
        }

        public static class FrameDataBGR extends FrameData {
            public NeuQuant neuQuant;

            public FrameDataBGR(BufferedImage image, int quality) {
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
                    int index = neuQuant.map(
                            pixels[k++] & 0xff,
                            pixels[k++] & 0xff,
                            pixels[k++] & 0xff
                    );
                    usedEntry[index] = true;
                    indexedPixels[i] = (byte) index;
                }
            }
        }

        public static class FrameDataABGR extends FrameData {
            public NeuQuantABGR neuQuant;

            public FrameDataABGR(BufferedImage image, int quality) {
                pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                // abgr
                byte[] transPixel = null;
                for (int i = 0; i < pixels.length; i += 4) {
                    if (pixels[i] != (0)) {
                        pixels[i] = (byte) 0xFF;
                    } else {
                        pixels[i] = 0;
                        pixels[i + 1] = 0;
                        pixels[i + 2] = 0;
                        pixels[i + 3] = 0;
                        if (transPixel == null) {
                            transPixel = new byte[]{
                                    0, 0, 0, 0
                            };
                        }
                    }
                }
                neuQuant = new NeuQuantABGR(quality, pixels);
                colorTab = neuQuant.colorMapBgr();
                // convert map from BGR to RGB
                for (int i = 0; i < colorTab.length; i += 3) {
                    byte temp = colorTab[i];
                    colorTab[i] = colorTab[i + 2];
                    colorTab[i + 2] = temp;
                    usedEntry[i / 3] = false;
                }

                int nPix = pixels.length / 4;
                indexedPixels = new byte[nPix];

                int k = 0;
                for (int i = 0; i < nPix; i++) {
                    int index = neuQuant.searchNetindexAbgr(
                            pixels[k++] & 0xff,
                            pixels[k++] & 0xff,
                            pixels[k++] & 0xff,
                            pixels[k++] & 0xff
                    );
                    usedEntry[index] = true;
                    indexedPixels[i] = (byte) index;
                }

                if (transPixel != null) {
                    transIndex = neuQuant.indexOf(transPixel);
                }
            }
        }

        // TODO: FrameDataIndexed
    }
}
