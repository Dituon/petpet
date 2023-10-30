package moe.dituon.petpet.share;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ImageSynthesis extends ImageSynthesisCore {
    protected static ExecutorService threadPool = Executors.newFixedThreadPool(BasePetService.DEFAULT_THREAD_POOL_SIZE);

    protected static void g2dDrawAvatar(Graphics2D g2d, AvatarModel avatar, short index) {
        g2dDrawAvatar(g2d, avatar, index, 1.0F);
    }

    protected static void g2dDrawAvatar(Graphics2D g2d, AvatarModel avatar,
                                        short index, float multiple) {
        switch (avatar.getPosType()) {
            case ZOOM:
                g2dDrawZoomAvatar(
                        g2d, avatar.getFrame(index), avatar.getPos(index),
                        avatar.getAngle(index),
                        avatar.getTransformOrigin() == TransformOrigin.CENTER,
                        multiple, avatar.getZoomType(), avatar.getOpacity()
                );
                break;
            case DEFORM:
                AvatarModel.DeformData deformData = avatar.getDeformData();
                g2dDrawDeformAvatar(g2d, avatar.getFrame(index),
                        deformData.getDeformPos(index), deformData.getAnchor(index));
                break;
        }
    }

    protected static void g2dDrawTexts(Graphics2D g2d, List<TextModel> texts,
                                       int stickerWidth, int stickerHeight) {
        if (texts == null || texts.isEmpty()) return;
        texts.forEach(text -> text.drawAsG2d(g2d, stickerWidth, stickerHeight));
    }

    public static BufferedImage synthesisImage(BufferedImage sticker,
                                               List<AvatarModel> avatarList, List<TextModel> textList,
                                               boolean antialias) {
        return synthesisImage(sticker, avatarList, textList, antialias, false, (short) 0, null);
    }

    public static BufferedImage synthesisImage(BufferedImage sticker,
                                               List<AvatarModel> avatarList, List<TextModel> textList,
                                               boolean antialias, boolean transparent) {
        return synthesisImage(sticker, avatarList, textList, antialias, transparent, (short) 0, null);
    }

    public static BufferedImage synthesisImage(BufferedImage sticker,
                                               List<AvatarModel> avatarList, List<TextModel> textList,
                                               boolean antialias, boolean transparent, short index) {
        return synthesisImage(sticker, avatarList, textList, antialias, transparent, index, null);
    }

    public static BufferedImage synthesisImage
            (BufferedImage sticker, List<AvatarModel> avatarList, List<TextModel> textList,
             boolean antialias, boolean transparent, short index, List<Integer> maxSize) {
        int stickerWidth = sticker.getWidth();
        int stickerHeight = sticker.getHeight();

        float multiple = 1.0F;
        if (maxSize != null && !maxSize.isEmpty()) {
            boolean zoom = false;
            if (maxSize.get(2) != null) {
                for (AvatarModel avatar : avatarList) {
                    if (avatar.getImageList().size() >= maxSize.get(2)) {
                        zoom = true;
                        break;
                    }
                }
            }

            if (zoom) {
                if (stickerWidth > maxSize.get(0))
                    multiple = (float) maxSize.get(0) / sticker.getWidth();
                if (stickerHeight > maxSize.get(1))
                    multiple = Math.min(multiple, (float) maxSize.get(1) / sticker.getHeight());
                stickerWidth = (int) (stickerWidth * multiple);
                stickerHeight = (int) (stickerHeight * multiple);
            }
        }

        BufferedImage output = new BufferedImage(stickerWidth, stickerHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = output.createGraphics();

        if (antialias) { //抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        // 背景
        if (transparent) {
            output = g2d.getDeviceConfiguration().createCompatibleImage(
                    stickerWidth, stickerHeight, Transparency.TRANSLUCENT);
            g2d.dispose();
            g2d = output.createGraphics();
        } else {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, stickerWidth, stickerHeight);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0F));
        }

        // 按照图层分类
        List<AvatarModel> topAvatars = new ArrayList<>(4);
        List<AvatarModel> bottomAvatars = new ArrayList<>(4);
        for (AvatarModel avatar : avatarList) {
            if (avatar.isOnTop()) {
                topAvatars.add(avatar);
            } else {
                bottomAvatars.add(avatar);
            }
        }
        // 画
        for (AvatarModel avatar : bottomAvatars) {
            g2dDrawAvatar(g2d, avatar, index, multiple);
        }
        g2d.drawImage(sticker, 0, 0, stickerWidth, stickerHeight, null);
        for (AvatarModel avatar : topAvatars) {
            g2dDrawAvatar(g2d, avatar, index, multiple);
        }

        g2dDrawTexts(g2d, textList, stickerWidth, stickerHeight);
        g2d.dispose();
        return output;
    }

    public static BufferedImage cropImage(BufferedImage image, CropType type, int[] cropPos) {
        return cropImage(image, cropPos, type == CropType.PERCENT);
    }

    static List<BufferedImage> execImageList(
            List<BufferedImage> imageList,
            Function<BufferedImage, BufferedImage> fun
    ) {
        return execImageList(imageList, ((i, image) -> fun.apply(image)));
    }

    static List<BufferedImage> execImageList(
            List<BufferedImage> imageList,
            BiFunction<Integer, BufferedImage, BufferedImage> fun
    ) {
        try {
            CountDownLatch latch = new CountDownLatch(imageList.size());
            BufferedImage[] result = new BufferedImage[imageList.size()];

            for (int i = 0; i < imageList.size(); i++) {
                var fi = i;
                threadPool.execute(() -> {
                    BufferedImage img = imageList.get(fi);
                    result[fi] = fun.apply(fi, img);
                    latch.countDown();
                });
            }

            latch.await();
            return Arrays.asList(result);
        } catch (InterruptedException ex){
            throw new RuntimeException(ex);
        }
    }
}