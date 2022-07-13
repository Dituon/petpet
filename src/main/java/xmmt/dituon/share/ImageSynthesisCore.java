package xmmt.dituon.share;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageSynthesisCore {
    public static void g2dDrawZoomAvatar(Graphics2D g2d, BufferedImage avatarImage, int[] pos,
                                         float angle, boolean antialias) {
        if (avatarImage == null) {
            return;
        }

        int x = pos[0];
        int y = pos[1];
        int w = pos[2];
        int h = pos[3];

        g2d.drawImage(rotateImage(avatarImage, angle, antialias), x, y, w, h, null);
    }



    public static void g2dDrawDeformAvatar(Graphics2D g2d, BufferedImage avatarImage, Point2D[] point, int[] anchorPos) {
        BufferedImage result = ImageDeformer.computeImage(avatarImage, point);
        g2d.drawImage(result, anchorPos[0], anchorPos[1], null);
    }

    public static void g2dDrawText(Graphics2D g2d, String text, int[] pos, Color color, Font font) {
        g2d.setColor(color);
        g2d.setFont(font);
        g2d.drawString(text, pos[0], pos[1]);
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

    /**
     * 旋转。实现里对于特殊角度有特殊处理分支。
     */
    public static BufferedImage rotateImage(BufferedImage originImage, float angle, boolean antialias) {
        if (angle == 0) {
            return originImage;
/*        } else if (angle % 90 == 0) {
            BufferedImage newImage = new BufferedImage(originImage.getWidth(), originImage.getHeight(), originImage.getType());
            Graphics2D rotateG2d = newImage.createGraphics();
            rotateG2d.rotate(Math.toRadians(angle), originImage.getWidth() / 2.0, originImage.getHeight() / 2.0);
            rotateG2d.drawImage(originImage, null, 0, 0);
            rotateG2d.dispose();
            return newImage;*/
        } else {
            return rotateNormalAngleImage(originImage, angle, antialias);
        }
    }

    /**
     * 旋转。实现里统一对待任意输入角度。
     */
    public static BufferedImage rotateNormalAngleImage(BufferedImage avatarImage, float angle, boolean antialias) {
        double sin = Math.abs(Math.sin(Math.toRadians(angle))),
                cos = Math.abs(Math.cos(Math.toRadians(angle)));
        int w = avatarImage.getWidth();
        int h = avatarImage.getHeight();
        int neww = (int) Math.floor(w * cos + h * sin),
                newh = (int) Math.floor(h * cos + w * sin);
        BufferedImage rotated = new BufferedImage(neww, newh, avatarImage.getType());
        Graphics2D g2d = rotated.createGraphics();
        if (antialias) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
/*        rotated = g2d.getDeviceConfiguration().createCompatibleImage(
                rotated.getWidth(), rotated.getHeight(), Transparency.TRANSLUCENT);
        g2d.dispose();
        g2d = rotated.createGraphics();*/

        g2d.translate((neww - w) / 2, (newh - h) / 2);
        g2d.rotate(Math.toRadians(angle), w / 2.0, h / 2.0);
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
