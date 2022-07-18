package xmmt.dituon.share;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ImageSynthesisCore {
    protected static void drawAvatar(Graphics2D g2d, AvatarModel avatar) {
        switch (avatar.getPosType()) {
            case ZOOM:
                g2dDrawZoomAvatar(g2d, avatar.getImage(),
                        avatar.nextPos(), avatar.getNextAngle(), avatar.isRound());
                break;
            case DEFORM:
                g2dDrawDeformAvatar(g2d, avatar.getImage(), avatar.getDeformData());
        }
    }

    protected static void g2dDrawZoomAvatar(Graphics2D g2d, BufferedImage avatarImage, int[] pos,
                                          float angle, boolean isRound) {
        if (avatarImage == null) {
            return;
        }

        int x = pos[0];
        int y = pos[1];
        int w = pos[2];
        int h = pos[3];
        if (angle == 0) {
            g2d.drawImage(avatarImage, x, y, w, h, null);
            return;
        }

        if (isRound || angle % 90 == 0) {
            BufferedImage newAvatarImage = new BufferedImage(avatarImage.getWidth(), avatarImage.getHeight(), avatarImage.getType());
            Graphics2D rotateG2d = newAvatarImage.createGraphics();
            rotateG2d.rotate(Math.toRadians(angle), avatarImage.getWidth() / 2, avatarImage.getHeight() / 2);
            rotateG2d.drawImage(avatarImage, null, 0, 0);
            rotateG2d.dispose();
            g2d.drawImage(newAvatarImage, x, y, w, h, null);
            return;
        }

        g2d.drawImage(rotateImage(avatarImage, angle), x, y, w, h, null);
    }

    protected static void g2dDrawDeformAvatar(Graphics2D g2d, BufferedImage avatarImage, AvatarModel.DeformData deformData) {
        BufferedImage result = ImageDeformer.computeImage(avatarImage, deformData.getDeformPos());
        g2d.drawImage(result, deformData.getAnchor()[0], deformData.getAnchor()[1], null);
    }

    protected static void g2dDrawTexts(Graphics2D g2d, ArrayList<TextModel> texts) {
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (texts == null || texts.isEmpty()) {
            return;
        }
        for (TextModel text : texts) {
            g2d.setColor(text.getColor());
            g2d.setFont(text.getFont());
            g2d.drawString(text.getText(), text.getPos()[0], text.getPos()[1]);
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

    public static BufferedImage getAvatarImage(String avatarUrl) {
        HttpURLConnection conn = null;
        BufferedImage image = null;
        try {
            URL url = new URL(avatarUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            image = ImageIO.read(conn.getInputStream());
            conn.disconnect();
        } catch (Exception e) {
            System.out.println("获取头像失败\nHttpURLConnection: " + conn + "\nURL: " + avatarUrl);
            e.printStackTrace();
        } finally {
            assert conn != null;
            conn.disconnect();
        }
        return image;
    }

    public static BufferedImage cropImage(BufferedImage image, CropType type, int[] cropPos) {
        int width = cropPos[2] - cropPos[0];
        int height = cropPos[3] - cropPos[1];
        if (type == CropType.PERCENT) {
            width = (int) ((float) width / 100 * image.getWidth());
            height = (int) ((float) height / 100 * image.getHeight());
        }
        BufferedImage croppedImage = new BufferedImage(width, height, image.getType());
        Graphics2D g2d = croppedImage.createGraphics();
        switch (type) {
            case PIXEL:
                g2d.drawImage(image, 0, 0, width, height
                        , cropPos[0], cropPos[1], cropPos[2], cropPos[3], null);
                break;
            case PERCENT:
                g2d.drawImage(image, 0, 0, width, height,
                        (int) ((float) cropPos[0] / 100 * image.getWidth()),
                        (int) ((float) cropPos[1] / 100 * image.getHeight()),
                        (int) ((float) cropPos[2] / 100 * image.getWidth()),
                        (int) ((float) cropPos[3] / 100 * image.getHeight()), null);
                break;
        }
        g2d.dispose();
        return croppedImage;
    }

    public static BufferedImage mirrorImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage mirroredImage;
        Graphics2D g2d;
        (g2d = (mirroredImage = new BufferedImage(width, height, image
                .getColorModel().getTransparency())).createGraphics())
                .drawImage(image, 0, 0, width, height, width, 0, 0, height, null);
        g2d.dispose();
        return mirroredImage;
    }

    public static BufferedImage flipImage(BufferedImage image) {
        BufferedImage flipped = new BufferedImage(image.getWidth(), image.getHeight(),
                image.getType());
        AffineTransform tran = AffineTransform.getTranslateInstance(0,
                image.getHeight());
        AffineTransform flip = AffineTransform.getScaleInstance(1d, -1d);
        tran.concatenate(flip);
        Graphics2D g2d = flipped.createGraphics();
        g2d.setTransform(tran);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return flipped;
    }

    public static BufferedImage grayImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage grayscaleImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int gray = (int) (color.getRed() * 0.299 + color.getGreen() * 0.587 + color.getBlue() * 0.114);
                Color color_end = new Color(gray, gray, gray);
                grayscaleImage.setRGB(x, y, color_end.getRGB());
            }
        }
        return grayscaleImage;
    }

    public static BufferedImage BinarizeImage(BufferedImage image) {
        int h = image.getHeight();
        int w = image.getWidth();
        BufferedImage binarizeImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int val = image.getRGB(i, j);
                int r = (0x00ff0000 & val) >> 16;
                int g = (0x0000ff00 & val) >> 8;
                int b = (0x000000ff & val);
                int m = (r + g + b);
                if (m >= 383) {
                    binarizeImage.setRGB(i, j, Color.WHITE.getRGB());
                } else {
                    binarizeImage.setRGB(i, j, 0);
                }
            }
        }
        return binarizeImage;
    }
}
