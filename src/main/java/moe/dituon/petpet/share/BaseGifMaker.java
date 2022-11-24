package moe.dituon.petpet.share;

import com.squareup.gifencoder.GifEncoder;
import com.squareup.gifencoder.Image;
import com.squareup.gifencoder.ImageOptions;
import moe.dituon.petpet.share.FastAnimatedGifEncoder.FrameData;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BaseGifMaker {
    protected ExecutorService threadPool;

    /**
     * 默认线程池容量为 <b>CPU线程数 + 1</b>
     */
    public BaseGifMaker() {
        threadPool =
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    }

    public BaseGifMaker(int threadPoolSize) {
        threadPool = Executors.newFixedThreadPool(threadPoolSize);
    }

    public InputStream makeGIF(ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
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

    public InputStream makeGifUseBufferedStream
            (ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
             HashMap<Short, BufferedImage> stickerMap, GifRenderParams params) {
        try {
            //遍历获取GIF长度(图片文件数量)
            short i = 0;
            CountDownLatch latch = new CountDownLatch(stickerMap.size());
            HashMap<Short, BufferedImage> imageMap = new HashMap<>(stickerMap.size());
            for (short key : stickerMap.keySet()) {
                short fi = i++;
                threadPool.execute(() -> {
                    BufferedImage image = ImageSynthesis.synthesisImage(
                            stickerMap.get(key), avatarList, textList,
                            params.getAntialias(), false, fi, params.getMaxSize()
                    );
                    BufferedImage temp =
                            new BufferedImage(image.getWidth(), image.getHeight(),
                                    BufferedImage.TYPE_3BYTE_BGR);
                    Graphics2D g = temp.createGraphics();
                    g.drawImage(image, 0, 0, null);
                    imageMap.put(fi, temp);
                    latch.countDown();
                });
            }
            BufferedGifEncoder gifEncoder =
                    new BufferedGifEncoder(BufferedImage.TYPE_3BYTE_BGR, params.getDelay(), true);
            latch.await();
            if (params.getReverse()) {
                var map = reverseMap(imageMap);
                for (i = 0; i < map.size(); i++) gifEncoder.addFrame(map.get(i));
            } else {
                for (i = 0; i < imageMap.size(); i++) gifEncoder.addFrame(imageMap.get(i));
            }
            gifEncoder.finish();
            return gifEncoder.getOutput();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream makeGifUseAnimatedLib
            (ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
             HashMap<Short, BufferedImage> stickerMap, GifRenderParams params) {
        try {
            //遍历获取GIF长度(图片文件数量)
            short i = 0;
            CountDownLatch latch = new CountDownLatch(stickerMap.size());
            HashMap<Short, FrameData> frameMap = new HashMap<>(stickerMap.size());
            int[] size = new int[2];
            for (short key : stickerMap.keySet()) {
                short fi = i++;
                threadPool.execute(() -> {
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
                });
            }

            FastAnimatedGifEncoder gifEncoder = new FastAnimatedGifEncoder();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            gifEncoder.start(output);
            gifEncoder.setRepeat(0);
            gifEncoder.setDelay(params.getDelay());
            gifEncoder.setQuality(100 - params.getQuality());

            latch.await();
            gifEncoder.setSize(size[0], size[1]);
            if (params.getReverse()) {
                var map = reverseMap(frameMap);
                map.forEach((id, frame) -> gifEncoder.addFrame(frame));
            } else {
                frameMap.forEach((id, frame) -> gifEncoder.addFrame(frame));
            }
            gifEncoder.finish();
            return new ByteArrayInputStream(output.toByteArray());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream makeGifUseSquareupLib(ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
                                             HashMap<Short, BufferedImage> stickerMap, GifRenderParams params) {
        try {
            short i = 0;
            CountDownLatch latch = new CountDownLatch(stickerMap.size());
            HashMap<Short, Image> imageMap = new HashMap<>(stickerMap.size());
            int[] size = new int[2];
            for (short key : stickerMap.keySet()) {
                short fi = i++;
                threadPool.execute(() -> {
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
                });
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageOptions options = new ImageOptions().setDelay(params.getDelay(), TimeUnit.MILLISECONDS);

            latch.await();
            GifEncoder gifEncoder = new GifEncoder(output, size[0], size[1], 0);
            if (params.getReverse()) {
                var map = reverseMap(imageMap);
                for (i = 0; i < map.size(); i++) gifEncoder.addImage(map.get(i), options);
            } else {
                for (i = 0; i < imageMap.size(); i++) gifEncoder.addImage(imageMap.get(i), options);
            }
            gifEncoder.finishEncoding();
            return new ByteArrayInputStream(output.toByteArray());
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream makeGIF(ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
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

    private InputStream makeGifUseBufferedStream(
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
                threadPool.execute(() -> {
                    BufferedImage image = ImageSynthesis.synthesisImage(
                            sticker, avatarList, textList,
                            params.getAntialias(), false, fi, params.getMaxSize()
                    );
                    BufferedImage temp =
                            new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
                    Graphics2D g = temp.createGraphics();
                    g.drawImage(image, 0, 0, null);
                    imageMap.put(fi, temp);
                    latch.countDown();
                });
            }

            BufferedGifEncoder gifEncoder =
                    new BufferedGifEncoder(BufferedImage.TYPE_3BYTE_BGR, params.getDelay(), true);
            latch.await();
            if (params.getReverse()) {
                var map = reverseMap(imageMap);
                for (short i = 0; i < map.size(); i++) gifEncoder.addFrame(map.get(i));
            } else {
                for (short i = 0; i < imageMap.size(); i++) gifEncoder.addFrame(imageMap.get(i));
            }
            gifEncoder.finish();
            return gifEncoder.getOutput();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream makeGifUseAnimatedLib(
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
                threadPool.execute(() -> {
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
                var map = reverseMap(frameMap);
                map.forEach((id, frame) -> gifEncoder.addFrame(frame));
            } else {
                frameMap.forEach((id, frame) -> gifEncoder.addFrame(frame));
            }
            gifEncoder.finish();
            return new ByteArrayInputStream(output.toByteArray());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream makeGifUseSquareupLib(ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
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
                threadPool.execute(() -> {
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
                });
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageOptions options = new ImageOptions().setDelay(params.getDelay(), TimeUnit.MILLISECONDS);

            latch.await();
            GifEncoder gifEncoder = new GifEncoder(output, size[0], size[1], 0);
            if (params.getReverse()) {
                var map = reverseMap(imageMap);
                for (short i = 0; i < map.size(); i++) gifEncoder.addImage(map.get(i), options);
            } else {
                for (short i = 0; i < imageMap.size(); i++) gifEncoder.addImage(imageMap.get(i), options);
            }
            gifEncoder.finishEncoding();
            return new ByteArrayInputStream(output.toByteArray());
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    static private <T> HashMap<Short, T> reverseMap(HashMap<Short, T> originMap) {
        int size = originMap.size();
        HashMap<Short, T> map = new HashMap<>(originMap.size());
        originMap.forEach((id, img) -> map.put((short) (size - id - 1), img));
        return map;
    }
}