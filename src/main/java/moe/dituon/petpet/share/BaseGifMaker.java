package moe.dituon.petpet.share;

import moe.dituon.petpet.share.FastAnimatedGifEncoder.FrameData;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class BaseGifMaker {

    /**
     * 默认线程池容量为 <b>CPU线程数 + 1</b>
     */
    public BaseGifMaker() {}

    public InputStream makeGIF(List<AvatarModel> avatarList, List<TextModel> textList,
                               BufferedImage[] stickers, GifRenderParams params) {
        switch (params.getEncoder()) {
            case ANIMATED_LIB:
                return makeGifUseAnimatedLib(avatarList, textList, stickers, params);
            case BUFFERED_STREAM:
                return makeGifUseBufferedStream(avatarList, textList, stickers, params);
        }
        throw new RuntimeException();
    }

    public InputStream makeGifUseBufferedStream(
            List<AvatarModel> avatarList, List<TextModel> textList,
            BufferedImage[] stickers, GifRenderParams params
    ) {
        try {
            //遍历获取GIF长度(图片文件数量)
            CountDownLatch latch = new CountDownLatch(stickers.length);
            BufferedImage[] images = new BufferedImage[stickers.length];
            for (int i = 0; i < stickers.length; i++) {
                int fi = i;
                ImageSynthesis.threadPool.execute(() -> {
                    BufferedImage image = ImageSynthesis.synthesisImage(
                            stickers[fi], avatarList, textList,
                            params.getAntialias(), false,
                            (short) fi, params.getMaxSize()
                    );
                    BufferedImage temp =
                            new BufferedImage(image.getWidth(), image.getHeight(),
                                    BufferedImage.TYPE_3BYTE_BGR);
                    Graphics2D g = temp.createGraphics();
                    g.drawImage(image, 0, 0, null);
                    images[fi] = temp;
                    latch.countDown();
                });
            }
            BufferedGifEncoder gifEncoder =
                    new BufferedGifEncoder(BufferedImage.TYPE_3BYTE_BGR, params.getDelay(), true);
            latch.await();
            if (params.getReverse()) {
                for (int s = images.length - 1; s >= 0; s--) gifEncoder.addFrame(images[s]);
            } else {
                for (BufferedImage image : images) gifEncoder.addFrame(image);
            }
            gifEncoder.finish();
            return gifEncoder.getOutput();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream makeGifUseAnimatedLib
            (List<AvatarModel> avatarList, List<TextModel> textList,
             BufferedImage[] stickers, GifRenderParams params) {
        try {
            //遍历获取GIF长度(图片文件数量)
            CountDownLatch latch = new CountDownLatch(stickers.length);
            FrameData[] frames = new FrameData[stickers.length];
            int[] size = new int[2];
            for (int i = 0; i < stickers.length; i++) {
                int fi = i;
                ImageSynthesis.threadPool.execute(() -> {
                    BufferedImage image = ImageSynthesis.synthesisImage(
                            stickers[fi], avatarList, textList,
                            params.getAntialias(), false, (short) fi, params.getMaxSize()
                    );
                    BufferedImage temp =
                            new BufferedImage(image.getWidth(), image.getHeight(),
                                    BufferedImage.TYPE_3BYTE_BGR);
                    Graphics2D g = temp.createGraphics();
                    g.drawImage(image, 0, 0, null);
                    g.dispose();
                    FrameData frameData = new FrameData(temp, params.getQuality());
                    frames[fi] = frameData;
                    if (fi == 0) {
                        size[0] = temp.getWidth();
                        size[1] = temp.getHeight();
                    }
                    latch.countDown();
                });
            }

            FastAnimatedGifEncoder gifEncoder = new FastAnimatedGifEncoder();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            gifEncoder.start(output);
            gifEncoder.setRepeat(0);
            gifEncoder.setDelay(params.getDelay());
            gifEncoder.setQuality(params.getQuality());

            latch.await();
            gifEncoder.setSize(size[0], size[1]);
            if (params.getReverse()) {
                for (int s = frames.length - 1; s >= 0; s--) gifEncoder.addFrame(frames[s]);
            } else {
                for (FrameData frame : frames) gifEncoder.addFrame(frame);
            }
            gifEncoder.finish();
            return new ByteArrayInputStream(output.toByteArray());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream makeGIF(List<AvatarModel> avatarList, List<TextModel> textList,
                               BufferedImage sticker, GifRenderParams params) {
        switch (params.getEncoder()) {
            case ANIMATED_LIB:
                return makeGifUseAnimatedLib(avatarList, textList, sticker, params);
            case BUFFERED_STREAM:
                return makeGifUseBufferedStream(avatarList, textList, sticker, params);
        }
        throw new RuntimeException();
    }

    private InputStream makeGifUseBufferedStream(
            List<AvatarModel> avatarList, List<TextModel> textList,
            BufferedImage sticker, GifRenderParams params) {
        try {
            int maxFrameLength = 1;
            for (AvatarModel avatar : avatarList) {
                maxFrameLength = Math.max(maxFrameLength, avatar.getImageList().size());
            }

            CountDownLatch latch = new CountDownLatch(maxFrameLength);
            BufferedImage[] images = new BufferedImage[maxFrameLength];
            for (short i = 0; i < maxFrameLength; i++) {
                short fi = i;
                ImageSynthesis.threadPool.execute(() -> {
                    BufferedImage image = ImageSynthesis.synthesisImage(
                            sticker, avatarList, textList,
                            params.getAntialias(), false, fi, params.getMaxSize()
                    );
                    BufferedImage temp =
                            new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
                    Graphics2D g = temp.createGraphics();
                    g.drawImage(image, 0, 0, null);
                    images[fi] = temp;
                    latch.countDown();
                });
            }

            BufferedGifEncoder gifEncoder =
                    new BufferedGifEncoder(BufferedImage.TYPE_3BYTE_BGR, params.getDelay(), true);
            latch.await();
            if (params.getReverse()) {
                for (int s = images.length - 1; s >= 0; s--) gifEncoder.addFrame(images[s]);
            } else {
                for (BufferedImage image : images) gifEncoder.addFrame(image);
            }
            gifEncoder.finish();
            return gifEncoder.getOutput();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream makeGifUseAnimatedLib(
            List<AvatarModel> avatarList, List<TextModel> textList,
            BufferedImage sticker, GifRenderParams params) {
        try {
            int maxFrameLength = 1;
            for (AvatarModel avatar : avatarList) {
                maxFrameLength = Math.max(maxFrameLength, avatar.getImageList().size());
            }

            CountDownLatch latch = new CountDownLatch(maxFrameLength);
            FrameData[] frames = new FrameData[maxFrameLength];
            int[] size = new int[2];
            for (short i = 0; i < maxFrameLength; i++) {
                short fi = i;
                ImageSynthesis.threadPool.execute(() -> {
                    BufferedImage image = ImageSynthesis.synthesisImage(
                            sticker, avatarList, textList,
                            params.getAntialias(), false, fi, params.getMaxSize()
                    );
                    BufferedImage temp =
                            new BufferedImage(image.getWidth(), image.getHeight(),
                                    BufferedImage.TYPE_3BYTE_BGR);
                    Graphics2D g = temp.createGraphics();
                    g.drawImage(image, 0, 0, null);
                    FrameData frameData = new FrameData(temp, params.getQuality());
                    frames[fi] = frameData;
                    if (fi == 0) {
                        size[0] = temp.getWidth();
                        size[1] = temp.getHeight();
                    }
                    latch.countDown();
                });
            }

            FastAnimatedGifEncoder gifEncoder = new FastAnimatedGifEncoder();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            gifEncoder.start(output);
            gifEncoder.setDelay(params.getDelay());
            gifEncoder.setRepeat(0);
            gifEncoder.setQuality(params.getQuality());

            latch.await();
            gifEncoder.setSize(size[0], size[1]);
            if (params.getReverse()) {
                for (int s = frames.length - 1; s >= 0; s--) gifEncoder.addFrame(frames[s]);
            } else {
                for (FrameData frame : frames) gifEncoder.addFrame(frame);
            }
            gifEncoder.finish();
            return new ByteArrayInputStream(output.toByteArray());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}