package xmmt.dituon.share;



import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageSynthesis {

    // 单头像合成，不旋转
    public static BufferedImage synthesisImage(BufferedImage avatarImage, BufferedImage sticker, int[] pos, boolean isAvatarOnTop) {
        return synthesisImage(avatarImage, sticker, pos, 0, isAvatarOnTop);
    }

    // 两个头像合成，不旋转
    public static BufferedImage synthesisImage(BufferedImage sticker, BufferedImage avatarImage1, BufferedImage avatarImage2,
                                                int[] pos1, int[] pos2, boolean isAvatarOnTop) {
        return synthesisImage(sticker, avatarImage1, avatarImage2, pos1, pos2, 0, isAvatarOnTop);
    }

    // 单头像合成，旋转
    public static BufferedImage synthesisImage(BufferedImage avatarImage, BufferedImage sticker, int[] pos,
                                                int rotateIndex, boolean isAvatarOnTop) {
        BufferedImage newAvatarImage = new BufferedImage(avatarImage.getWidth(), avatarImage.getHeight(), avatarImage.getType());
        if (rotateIndex != 0) {
            Graphics2D rotateG2d = newAvatarImage.createGraphics();
            rotateG2d.rotate(Math.toRadians((float) (360 / pos.length) * rotateIndex),
                    avatarImage.getWidth() / 2, avatarImage.getHeight() / 2);
            rotateG2d.drawImage(avatarImage, null, 0, 0);
        } else {
            newAvatarImage = avatarImage;
        }

        int x = pos[0];
        int y = pos[1];
        int w = pos[2];
        int h = pos[3];
        BufferedImage output = new BufferedImage(sticker.getWidth(), sticker.getHeight(), sticker.getType());
        Graphics2D g2d = output.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, sticker.getWidth(), sticker.getHeight());
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0F));
        if (isAvatarOnTop) {
            g2d.drawImage(sticker, 0, 0, sticker.getWidth(), sticker.getHeight(), null);
            g2d.drawImage(newAvatarImage, x, y, w, h, null);
        } else {
            g2d.drawImage(newAvatarImage, x, y, w, h, null);
            g2d.drawImage(sticker, 0, 0, sticker.getWidth(), sticker.getHeight(), null);
        }
        g2d.dispose();
        return output;
    }

    // 两个头像合成，旋转
    public static BufferedImage synthesisImage(BufferedImage sticker, BufferedImage avatarImage1, BufferedImage avatarImage2,
                                                int[] pos1, int[] pos2,
                                                int rotateIndex, boolean isAvatarOnTop) {
        BufferedImage newAvatarImage1 = new BufferedImage(avatarImage1.getWidth(), avatarImage1.getHeight(), avatarImage1.getType());
        BufferedImage newAvatarImage2 = new BufferedImage(avatarImage1.getWidth(), avatarImage1.getHeight(), avatarImage1.getType());

        if (rotateIndex != 0) {
            Graphics2D rotateG2d1 = newAvatarImage1.createGraphics();
            rotateG2d1.rotate(Math.toRadians((float) (360 / pos1.length) * (rotateIndex + 1)),
                    avatarImage1.getWidth() / 2, avatarImage1.getHeight() / 2);
            rotateG2d1.drawImage(avatarImage1, null, 0, 0);

            Graphics2D rotateG2d2 = newAvatarImage2.createGraphics();
            rotateG2d2.rotate(Math.toRadians((float) (360 / pos1.length) * (rotateIndex + 1)),
                    avatarImage2.getWidth() / 2, avatarImage2.getHeight() / 2);
            rotateG2d2.drawImage(avatarImage2, null, 0, 0);
        } else {
            newAvatarImage1 = avatarImage1;
            newAvatarImage2 = avatarImage2;
        }

        int x1 = pos1[0];
        int y1 = pos1[1];
        int w1 = pos1[2];
        int h1 = pos1[3];

        int x2 = pos2[0];
        int y2 = pos2[1];
        int w2 = pos2[2];
        int h2 = pos2[3];

        BufferedImage output = new BufferedImage(sticker.getWidth(), sticker.getHeight(), sticker.getType());
        Graphics2D g2d = output.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, sticker.getWidth(), sticker.getHeight());
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0F));
        if (isAvatarOnTop) {
            g2d.drawImage(sticker, 0, 0, sticker.getWidth(), sticker.getHeight(), null);
            g2d.drawImage(newAvatarImage1, x1, y1, w1, h1, null);
            g2d.drawImage(newAvatarImage2, x2, y2, w2, h2, null);
        } else {
            g2d.drawImage(newAvatarImage1, x1, y1, w1, h1, null);
            g2d.drawImage(newAvatarImage2, x2, y2, w2, h2, null);
            g2d.drawImage(sticker, 0, 0, sticker.getWidth(), sticker.getHeight(), null);
        }
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