package xmmt.dituon.share;

import com.squareup.gifencoder.GifEncoder;
import com.squareup.gifencoder.Image;
import com.squareup.gifencoder.ImageOptions;
import xmmt.dituon.share.FastAnimatedGifEncoder.FrameData;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class BaseGifMaker {
    public static InputStream makeGIF(ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
                                      HashMap<Short, BufferedImage> stickerMap, GifRenderParams params) {
        switch (params.getEncoder()) {
            case ANIMATED_LIB:
                return makeGifUseAnimatedLib(avatarList, textList, stickerMap, params);
            case BUFFERED_STREAM:
                return makeGifUseBufferedStream(avatarList, textList, stickerMap, params);
            case SQUAREUP_LIB:
                return makeGifUseSquareupLib(avatarList, textList, stickerMap, params);
        }
        return null;
    }

    public static InputStream makeGifUseBufferedStream
            (ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
             HashMap<Short, BufferedImage> stickerMap, GifRenderParams params) {
        try {
            //遍历获取GIF长度(图片文件数量)
            short i = 0;
            CountDownLatch latch = new CountDownLatch(stickerMap.size());
            HashMap<Short, BufferedImage> imageMap = new HashMap<>();
            for (short key : stickerMap.keySet()) {
                short fi = i++;
                new Thread(() -> {
                    imageMap.put(fi, ImageSynthesis.synthesisImage(
                            stickerMap.get(key), avatarList, textList,
                            params.getAntialias(), false, fi, params.getMaxSize()));
                    latch.countDown();
                }).start();
            }
            BufferedGifEncoder gifEncoder =
                    new BufferedGifEncoder(stickerMap.get((short) 0).getType(), params.getDelay(), true);
            latch.await();
            for (i = 0; i < imageMap.size(); i++) gifEncoder.addFrame(imageMap.get(i));
            gifEncoder.finish();
            return gifEncoder.getOutput();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream makeGifUseAnimatedLib
            (ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
             HashMap<Short, BufferedImage> stickerMap, GifRenderParams params) {
        try {
            //遍历获取GIF长度(图片文件数量)
            short i = 0;
            CountDownLatch latch = new CountDownLatch(stickerMap.size());
            HashMap<Short, FrameData> frameMap = new HashMap<>();
            int[] size = new int[2];
            for (short key : stickerMap.keySet()) {
                short fi = i++;
                new Thread(() -> {
                    BufferedImage image = ImageSynthesis.synthesisImage(
                            stickerMap.get(key), avatarList, textList,
                            params.getAntialias(), false, fi, params.getMaxSize()
                    );
                    BufferedImage temp =
                            new BufferedImage(image.getWidth(), image.getHeight(),
                                    BufferedImage.TYPE_3BYTE_BGR);
                    Graphics2D g = temp.createGraphics();
                    g.drawImage(image, 0, 0, null);
                    FrameData frameData = new FrameData(temp, params.getQuality());
                    frameMap.put(fi, frameData);
                    if (fi == 0) {
                        size[0] = temp.getWidth();
                        size[1] = temp.getHeight();
                    }
                    latch.countDown();
                }).start();
            }

            FastAnimatedGifEncoder gifEncoder = new FastAnimatedGifEncoder();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            gifEncoder.start(output);
            gifEncoder.setRepeat(0);
            gifEncoder.setDelay(params.getDelay());
            gifEncoder.setQuality(100 - params.getQuality());

            latch.await();
            gifEncoder.setSize(size[0], size[1]);
            frameMap.forEach((in, frame) -> gifEncoder.addFrame(frame));
            gifEncoder.finish();
            return new ByteArrayInputStream(output.toByteArray());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream makeGifUseSquareupLib(ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
                                                    HashMap<Short, BufferedImage> stickerMap, GifRenderParams params) {
        try {
            short i = 0;
            CountDownLatch latch = new CountDownLatch(stickerMap.size());
            HashMap<Short, Image> imageMap = new HashMap<>();
            int[] size = new int[2];
            for (short key : stickerMap.keySet()) {
                short fi = i++;
                new Thread(() -> {
                    BufferedImage image = ImageSynthesis.synthesisImage(
                            stickerMap.get(key), avatarList, textList,
                            params.getAntialias(), false, fi, params.getMaxSize());
                    if (fi == 0) {
                        size[0] = image.getWidth();
                        size[1] = image.getHeight();
                    }
                    Image rgb = Image.fromRgb(ImageSynthesis.convertImageToArray(image));
                    imageMap.put(fi, rgb);
                    latch.countDown();
                }).start();
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageOptions options = new ImageOptions().setDelay(params.getDelay(), TimeUnit.MILLISECONDS);

            latch.await();
            GifEncoder gifEncoder = new GifEncoder(output, size[0], size[1], 0);
            imageMap.forEach((in, image) -> {
                try {
                    gifEncoder.addImage(image, options);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            gifEncoder.finishEncoding();
            return new ByteArrayInputStream(output.toByteArray());
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream makeGIF(ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
                                      BufferedImage sticker, GifRenderParams params) {
        switch (params.getEncoder()) {
            case ANIMATED_LIB:
                return makeGifUseAnimatedLib(avatarList, textList, sticker, params);
            case BUFFERED_STREAM:
                return makeGifUseBufferedStream(avatarList, textList, sticker, params);
            case SQUAREUP_LIB:
                return makeGifUseSquareupLib(avatarList, textList, sticker, params);
        }
        return null;
    }

    private static InputStream makeGifUseBufferedStream(
            ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
            BufferedImage sticker, GifRenderParams params) {
        try {
            short maxFrameLength = 1;
            for (AvatarModel avatar : avatarList) {
                maxFrameLength = (short) Math.max(maxFrameLength, avatar.getImageList().size());
            }

            CountDownLatch latch = new CountDownLatch(maxFrameLength);
            HashMap<Short, BufferedImage> imageMap = new HashMap<>();
            for (short i = 0; i < maxFrameLength; i++) {
                short fi = i;
                new Thread(() -> {
                    imageMap.put(fi, ImageSynthesis.synthesisImage(
                            sticker, avatarList, textList,
                            params.getAntialias(), false, fi, params.getMaxSize()));
                    latch.countDown();
                }).start();
            }

            BufferedGifEncoder gifEncoder =
                    new BufferedGifEncoder(sticker.getType(), params.getDelay(), true);
            latch.await();
            for (short i = 0; i < imageMap.size(); i++) gifEncoder.addFrame(imageMap.get(i));
            gifEncoder.finish();
            return gifEncoder.getOutput();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream makeGifUseAnimatedLib(
            ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
            BufferedImage sticker, GifRenderParams params) {
        try {
            short maxFrameLength = 1;
            for (AvatarModel avatar : avatarList) {
                maxFrameLength = (short) Math.max(maxFrameLength, avatar.getImageList().size());
            }

            CountDownLatch latch = new CountDownLatch(maxFrameLength);
            HashMap<Short, FrameData> frameMap = new HashMap<>();
            int[] size = new int[2];
            for (short i = 0; i < maxFrameLength; i++) {
                short fi = i;
                new Thread(() -> {
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
                    frameMap.put(fi, frameData);
                    if (fi == 0) {
                        size[0] = temp.getWidth();
                        size[1] = temp.getHeight();
                    }
                    latch.countDown();
                }).start();
            }

            FastAnimatedGifEncoder gifEncoder = new FastAnimatedGifEncoder();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            gifEncoder.start(output);
            gifEncoder.setDelay(params.getDelay());
            gifEncoder.setRepeat(0);
            gifEncoder.setQuality(params.getQuality());

            latch.await();
            gifEncoder.setSize(size[0], size[1]);
            frameMap.forEach((i, frame) -> gifEncoder.addFrame(frame));
            gifEncoder.finish();
            return new ByteArrayInputStream(output.toByteArray());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream makeGifUseSquareupLib(ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
                                                    BufferedImage sticker, GifRenderParams params) {
        try {
            short maxFrameLength = 1;
            for (AvatarModel avatar : avatarList) {
                maxFrameLength = (short) Math.max(maxFrameLength, avatar.getImageList().size());
            }
            CountDownLatch latch = new CountDownLatch(maxFrameLength);
            HashMap<Short, Image> imageMap = new HashMap<>();
            int[] size = new int[2];
            for (short i = 0; i < maxFrameLength; i++) {
                short fi = i;
                new Thread(() -> {
                    BufferedImage image = ImageSynthesis.synthesisImage(
                            sticker, avatarList, textList,
                            params.getAntialias(), false, fi, params.getMaxSize());
                    if (fi == 0) {
                        size[0] = image.getWidth();
                        size[1] = image.getHeight();
                    }
                    Image rgb = Image.fromRgb(ImageSynthesis.convertImageToArray(image));
                    imageMap.put(fi, rgb);
                    latch.countDown();
                }).start();
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageOptions options = new ImageOptions().setDelay(params.getDelay(), TimeUnit.MILLISECONDS);

            latch.await();
            GifEncoder gifEncoder = new GifEncoder(output,
                    size[0], size[1], 0);
            imageMap.forEach((in, image) -> {
                try {
                    gifEncoder.addImage(image, options);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            gifEncoder.finishEncoding();
            return new ByteArrayInputStream(output.toByteArray());
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
