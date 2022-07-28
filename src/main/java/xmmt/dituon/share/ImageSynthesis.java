package xmmt.dituon.share;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ImageSynthesis extends ImageSynthesisCore {
    protected static void drawAvatar(Graphics2D g2d, AvatarModel avatar) {
        switch (avatar.getPosType()) {
            case ZOOM:
                g2dDrawZoomAvatar(g2d, avatar.getImage(),
                        avatar.nextPos(), avatar.getNextAngle(), avatar.isRound());
                break;
            case DEFORM:
                AvatarModel.DeformData deformData = avatar.getDeformData();
                g2dDrawDeformAvatar(g2d, avatar.getImage(), deformData.getDeformPos(), deformData.getAnchor());
                break;
        }
    }

    protected static void g2dDrawTexts(Graphics2D g2d, ArrayList<TextModel> texts) {
        if (texts == null || texts.isEmpty()) {
            return;
        }
        for (TextModel text : texts) {
            ImageSynthesisCore.g2dDrawText(g2d, text.getText(), text.getPos(), text.getColor(), text.getFont());
        }
    }

    public static BufferedImage synthesisImage(BufferedImage sticker,
                                               ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
                                               boolean antialias) {
        return synthesisImage(sticker, avatarList, textList, antialias, false);
    }

    public static BufferedImage synthesisImage(BufferedImage sticker,
                                               ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
                                               boolean antialias, boolean transparent) {
        BufferedImage output = new BufferedImage(sticker.getWidth(), sticker.getHeight(), sticker.getType());
        Graphics2D g2d = output.createGraphics();

        if (antialias) { //抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        // 背景
        if (transparent) {
            output = g2d.getDeviceConfiguration().createCompatibleImage(
                    sticker.getWidth(), sticker.getHeight(), Transparency.TRANSLUCENT);
            g2d.dispose();
            g2d = output.createGraphics();
        } else {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, sticker.getWidth(), sticker.getHeight());
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0F));
        }

        // 按照图层分类
        ArrayList<AvatarModel> topAvatars = new ArrayList<>();
        ArrayList<AvatarModel> bottomAvatars = new ArrayList<>();
        for (AvatarModel avatar : avatarList) {
            if (avatar.isOnTop()) {
                topAvatars.add(avatar);
            } else {
                bottomAvatars.add(avatar);
            }
        }
        // 画
        for (AvatarModel avatar : bottomAvatars) {
            drawAvatar(g2d, avatar);
        }
        g2d.drawImage(sticker, 0, 0, sticker.getWidth(), sticker.getHeight(), null);
        for (AvatarModel avatar : topAvatars) {
            drawAvatar(g2d, avatar);
        }

        g2dDrawTexts(g2d, textList);
        g2d.dispose();
        return output;
    }

    public static BufferedImage cropImage(BufferedImage image, CropType type, int[] cropPos) {
        return ImageSynthesisCore.cropImage(image, cropPos, type == CropType.PERCENT);
    }
}