package xmmt.dituon.share;


import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ImageSynthesis {

    public static BufferedImage synthesisImage(BufferedImage sticker,
                                               ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
                                               boolean antialias) {
        BufferedImage output = new BufferedImage(sticker.getWidth(), sticker.getHeight(), sticker.getType());
        Graphics2D g2d = output.createGraphics();

        if (antialias) { //抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        // 背景
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, sticker.getWidth(), sticker.getHeight());
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0F));

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
            g2dDrawAvatar(g2d, avatar.getImage(), avatar.nextPos(), avatar.getRotateIndex(), avatar.getAngle());
        }
        g2d.drawImage(sticker, 0, 0, sticker.getWidth(), sticker.getHeight(), null);
        for (AvatarModel avatar : topAvatars) {
            g2dDrawAvatar(g2d, avatar.getImage(), avatar.nextPos(), avatar.getRotateIndex(), avatar.getAngle());
        }

        g2dDrawTexts(g2d, textList);
        g2d.dispose();
        return output;
    }

    private static void g2dDrawAvatar(Graphics2D g2d, BufferedImage avatarImage, int[] pos, int rotateIndex, int angle) {
        if (avatarImage == null) {
            return;
        }
        BufferedImage newAvatarImage = new BufferedImage(avatarImage.getWidth(), avatarImage.getHeight(), avatarImage.getType());

        if (rotateIndex == 0 && angle == 0){
            newAvatarImage = avatarImage;
        } else {
            Graphics2D rotateG2d1 = newAvatarImage.createGraphics();
            //TODO 旋转时会有黑边 应当使用AffineTransform
            rotateG2d1.rotate(Math.toRadians((float) ((360 / pos.length) * (rotateIndex)) + angle),
                    avatarImage.getWidth() / 2, avatarImage.getHeight() / 2);
            rotateG2d1.drawImage(avatarImage, null, 0, 0);
        }

        int x = pos[0];
        int y = pos[1];
        int w = pos[2];
        int h = pos[3];
        g2d.drawImage(newAvatarImage, x, y, w, h, null);
    }

    private static void g2dDrawTexts(Graphics2D g2d, ArrayList<TextModel> texts) {
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