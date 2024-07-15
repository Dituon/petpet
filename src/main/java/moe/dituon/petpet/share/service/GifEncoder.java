package moe.dituon.petpet.share.service;

import moe.dituon.petpet.share.BasePetService;
import moe.dituon.petpet.share.FastAnimatedGifEncoder;
import moe.dituon.petpet.share.GifRenderParams;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GifEncoder {
    protected static ExecutorService threadPool = Executors.newFixedThreadPool(BasePetService.DEFAULT_THREAD_POOL_SIZE);

    public static byte[] makeGifUseAnimatedLib(List<BufferedImage> images, GifEncoderParam params) {
        try {
            CountDownLatch latch = new CountDownLatch(images.size());
            FastAnimatedGifEncoder.FrameData[] frames = new FastAnimatedGifEncoder.FrameData[images.size()];
            for (int i = 0; i < images.size(); i++) {
                int fi = i;
                threadPool.execute(() -> {
                    var image = images.get(fi);
                    if (image.getType() == BufferedImage.TYPE_3BYTE_BGR) {
                        FastAnimatedGifEncoder.FrameData frameData = new FastAnimatedGifEncoder.FrameData(image, params.getQuality());
                        frames[fi] = frameData;
                        latch.countDown();
                        return;
                    }
                    BufferedImage temp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
                    Graphics2D g2d = temp.createGraphics();
                    g2d.drawImage(image, 0, 0, null);
                    g2d.dispose();
                    FastAnimatedGifEncoder.FrameData frameData = new FastAnimatedGifEncoder.FrameData(temp, params.getQuality());
                    frames[fi] = frameData;
                    latch.countDown();
                });
            }

            FastAnimatedGifEncoder gifEncoder = new FastAnimatedGifEncoder();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            gifEncoder.start(output);
            gifEncoder.setRepeat(params.getRepeat());
            gifEncoder.setDelay(params.getDelay());
            gifEncoder.setQuality(params.getQuality());

            latch.await();
            gifEncoder.setSize(images.get(0).getWidth(), images.get(0).getHeight());
            if (params.getReverse()) {
                for (int s = frames.length - 1; s >= 0; s--) gifEncoder.addFrame(frames[s]);
            } else {
                for (FastAnimatedGifEncoder.FrameData frame : frames) gifEncoder.addFrame(frame);
            }
            gifEncoder.finish();
            var array = output.toByteArray();
            output.close();
            return array;
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
