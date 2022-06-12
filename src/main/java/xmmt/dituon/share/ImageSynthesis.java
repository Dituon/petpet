package xmmt.dituon.share;


import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;

public class ImageSynthesis {


    /**
     * 至多两个（可null）头像合成，不旋转，带文字
     */
    public static BufferedImage synthesisImage(BufferedImage sticker, BufferedImage avatarImage1, BufferedImage avatarImage2,
                                               int[] pos1, int[] pos2, boolean isAvatarOnTop,boolean antialias, ArrayList<TextModel> texts) {
        return synthesisImage(sticker, avatarImage1, avatarImage2, pos1, pos2, 0, isAvatarOnTop, antialias, texts);
    }

    /**
     * 至多两个（可null）头像合成，旋转
     */
    public static BufferedImage synthesisImage(BufferedImage sticker, BufferedImage avatarImage1, BufferedImage avatarImage2,
                                               int[] pos1, int[] pos2,
                                               int rotateIndex, boolean isAvatarOnTop, boolean antialias) {
        return synthesisImage(sticker, avatarImage1, avatarImage2, pos1, pos2, rotateIndex, isAvatarOnTop, antialias, null);
    }

    // 至多两个（可null）头像合成，旋转，带文字
    public static BufferedImage synthesisImage(BufferedImage sticker, BufferedImage avatarImage1, BufferedImage avatarImage2,
                                               int[] pos1, int[] pos2,
                                               int rotateIndex, boolean isAvatarOnTop, boolean antialias,
                                               ArrayList<TextModel> texts) {
        return synthesisImageGeneral(sticker, avatarImage1, avatarImage2, pos1, pos2, rotateIndex, isAvatarOnTop, antialias, texts);
    }

    private static void g2dDrawAvatar(Graphics2D g2d, BufferedImage avatarImage, int[] pos, int rotateIndex) {
        if (avatarImage == null) {
            return;
        }
        BufferedImage newAvatarImage = new BufferedImage(avatarImage.getWidth(), avatarImage.getHeight(), avatarImage.getType());
        if (rotateIndex != 0) {
            Graphics2D rotateG2d1 = newAvatarImage.createGraphics();
            rotateG2d1.rotate(Math.toRadians((float) (360 / pos.length) * (rotateIndex + 1)),
                    avatarImage.getWidth() / 2, avatarImage.getHeight() / 2);
            rotateG2d1.drawImage(avatarImage, null, 0, 0);
        } else {
            newAvatarImage = avatarImage;
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

    private static BufferedImage synthesisImageGeneral(
            BufferedImage sticker,
            @Nullable BufferedImage avatarImage1,
            @Nullable BufferedImage avatarImage2,
            @Nullable int[] pos1, @Nullable int[] pos2,
            int rotateIndex,
            boolean isAvatarOnTop,
            boolean antialias,
            @Nullable ArrayList<TextModel> texts
    ) {

        BufferedImage output = new BufferedImage(sticker.getWidth(), sticker.getHeight(), sticker.getType());
        Graphics2D g2d = output.createGraphics();

        if (antialias) { //抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        // 背景
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, sticker.getWidth(), sticker.getHeight());
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0F));
        // 按图层顺序依次添加，方便直观调整顺序
        if (isAvatarOnTop) {
            g2d.drawImage(sticker, 0, 0, sticker.getWidth(), sticker.getHeight(), null);
            g2dDrawAvatar(g2d, avatarImage1, pos1, rotateIndex);
            g2dDrawAvatar(g2d, avatarImage2, pos2, rotateIndex);
        } else {
            g2dDrawAvatar(g2d, avatarImage1, pos1, rotateIndex);
            g2dDrawAvatar(g2d, avatarImage2, pos2, rotateIndex);
            g2d.drawImage(sticker, 0, 0, sticker.getWidth(), sticker.getHeight(), null);
        }
        g2dDrawTexts(g2d, texts);

        g2d.dispose();
        return output;
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