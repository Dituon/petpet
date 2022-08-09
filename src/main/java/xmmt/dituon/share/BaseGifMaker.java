package xmmt.dituon.share;

import com.madgag.gif.fmsware.AnimatedGifEncoder;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class BaseGifMaker {

    public static InputStream makeGIF(ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
                                      HashMap<Short, BufferedImage> stickerMap, boolean antialias) {
        try {
            //遍历获取GIF长度(图片文件数量)
            short i = 0;
            CountDownLatch latch = new CountDownLatch(stickerMap.size());
            HashMap<Short, BufferedImage> imageMap = new HashMap<>();
            for (short key : stickerMap.keySet()) {
                short fi = i++;
                new Thread(() -> {
                    imageMap.put(fi, ImageSynthesis.synthesisImage(
                            stickerMap.get(key), avatarList, textList, antialias, false, fi));
                    latch.countDown();
                }).start();
            }

            AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            gifEncoder.start(output);
            gifEncoder.setDelay(65);

            latch.await();
            imageMap.forEach((in, image) -> gifEncoder.addFrame(image));
            gifEncoder.finish();
            return new ByteArrayInputStream(output.toByteArray());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream makeGIF(ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
                                      BufferedImage sticker, boolean antialias) {
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
                            sticker, avatarList, textList, antialias, false, fi
                    ));
                    latch.countDown();
                }).start();
            }

            AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            gifEncoder.start(output);
            gifEncoder.setDelay(65);

            latch.await();
            imageMap.forEach((i, image) -> gifEncoder.addFrame(image));
            gifEncoder.finish();
            return new ByteArrayInputStream(output.toByteArray());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
