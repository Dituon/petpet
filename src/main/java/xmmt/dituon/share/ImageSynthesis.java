package xmmt.dituon.share;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ImageSynthesis {

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

    private static void drawAvatar(Graphics2D g2d, AvatarModel avatar) {
        switch (avatar.getPosType()) {
            case ZOOM:
                g2dDrawZoomAvatar(g2d, avatar.getImage(),
                        avatar.nextPos(), avatar.getRotateIndex(), avatar.getAngle(), avatar.isRound());
                break;
            case DEFORM:
                g2dDrawDeformAvatar(g2d, avatar.getImage(), avatar.getDeformData());
        }
    }

    private static void g2dDrawZoomAvatar(Graphics2D g2d, BufferedImage avatarImage, int[] pos,
                                          int rotateIndex, int angle, boolean isRound) {
        if (avatarImage == null) {
            return;
        }

        int x = pos[0];
        int y = pos[1];
        int w = pos[2];
        int h = pos[3];
        if (rotateIndex == 0 && angle == 0) {
            g2d.drawImage(avatarImage, x, y, w, h, null);
            return;
        }

        if (isRound || angle % 90 == 0) {
            BufferedImage newAvatarImage = new BufferedImage(avatarImage.getWidth(), avatarImage.getHeight(), avatarImage.getType());
            Graphics2D rotateG2d = newAvatarImage.createGraphics();
            rotateG2d.rotate(Math.toRadians(((float) (360 / pos.length) * (rotateIndex + 1)) + angle),
                    avatarImage.getWidth() / 2, avatarImage.getHeight() / 2);
            rotateG2d.drawImage(avatarImage, null, 0, 0);
            rotateG2d.dispose();
            g2d.drawImage(newAvatarImage, x, y, w, h, null);
            return;
        }

        g2d.drawImage(rotateImage(avatarImage,
                ((float) (360 / pos.length) * (rotateIndex + 1)) + angle), x, y, w, h, null);
    }

    private static void g2dDrawDeformAvatar(Graphics2D g2d, BufferedImage avatarImage, AvatarModel.DeformData deformData) {
        BufferedImage result = ImageDeformer.computeImage(avatarImage, deformData.getDeformPos());
        g2d.drawImage(result, deformData.getAnchor()[0], deformData.getAnchor()[1], null);
    }

    private static void g2dDrawTexts(Graphics2D g2d, ArrayList<TextModel> texts) {
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (texts != null && !texts.isEmpty()) {
            for (TextModel text : texts) {
                g2d.setColor(text.getColor());
                g2d.setFont(text.getFont());
                g2d.drawString(text.getText(), text.getPos()[0], text.getPos()[1]);
            }
        }
    }

    public static BufferedImage convertCircular(BufferedImage input, boolean antialias) throws IOException {
        BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Ellipse2D.Double shape = new Ellipse2D.Double(0, 0, input.getWidth(), input.getHeight());
        Graphics2D g2 = output.createGraphics();
        g2.setClip(shape);

        if (antialias) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        g2.drawImage(input, 0, 0, null);
        g2.dispose();
        return output;
    }

    public static BufferedImage rotateImage(BufferedImage avatarImage, float angle) {
        double sin = Math.abs(Math.sin(Math.toRadians(angle))),
                cos = Math.abs(Math.cos(Math.toRadians(angle)));
        int w = avatarImage.getWidth();
        int h = avatarImage.getHeight();
        int neww = (int) Math.floor(w * cos + h * sin),
                newh = (int) Math.floor(h * cos + w * sin);
        BufferedImage rotated = new BufferedImage(neww, newh, avatarImage.getType());
        Graphics2D g2d = rotated.createGraphics();
        rotated = g2d.getDeviceConfiguration().createCompatibleImage(
                rotated.getWidth(), rotated.getHeight(), Transparency.TRANSLUCENT);
        g2d.dispose();
        g2d = rotated.createGraphics();

        g2d.translate((neww - w) / 2, (newh - h) / 2);
        g2d.rotate(Math.toRadians(angle), w / 2, h / 2);
        g2d.drawRenderedImage(avatarImage, null);
        g2d.dispose();
        return rotated;
    }

    public static BufferedImage getAvatarImage(String URL) {
        HttpURLConnection conn = null;
        BufferedImage image = null;
        try {
            java.net.URL url = new URL(URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            image = ImageIO.read(conn.getInputStream());
            conn.disconnect();
        } catch (Exception e) {
            System.out.println("获取头像失败\nHttpURLConnection: " + conn + "\nURL: " + URL);
            e.printStackTrace();
        } finally {
            assert conn != null;
            conn.disconnect();
        }
        return image;
    }
}