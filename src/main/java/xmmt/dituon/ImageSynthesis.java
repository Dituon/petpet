package xmmt.dituon;

import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageSynthesis {
    public static Image sendImage(Member m, String path, int[][] pos, boolean isAvatarOnTop, boolean isRotate) {
        int i = 0;
        try {
            GifMaker gifMaker = new GifMaker(ImageIO.read(new File(path + "0.png")).getType(), 65, true);
            BufferedImage avatarImage = convertCircular(getAvatarImage(m.getAvatarUrl()));
            BufferedImage newAvatarImage = new BufferedImage(avatarImage.getWidth(), avatarImage.getHeight(), avatarImage.getType());
            for (int[] p : pos) {
                if (isRotate) {
                    Graphics2D rotateG2d = newAvatarImage.createGraphics();
                    rotateG2d.rotate(Math.toRadians((double) (360 / pos.length) * (i + 1)),
                            avatarImage.getWidth() / 2, avatarImage.getHeight() / 2);
                    rotateG2d.drawImage(avatarImage, null, 0, 0);
                } else {
                    newAvatarImage = avatarImage;
                }

                File f = new File(path + i + ".png");
                i++;
                BufferedImage sticker = ImageIO.read(f);
                int x = p[0];
                int y = p[1];
                int w = p[2];
                int h = p[3];
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
                gifMaker.writeToSequence(output);
            }
            gifMaker.close();
            ExternalResource resource = ExternalResource.create(gifMaker.getOutput());

            Image image = m.uploadImage(resource);
            resource.close();
            return image;
        } catch (IOException ex) {
            System.out.println("(sendImage)构造GIF失败，请检查 PetData.java");
            ex.printStackTrace();
        }
        return null;
    }

    public static Image sendImage(Member m1, Member m2, String path, int[][] pos1, int[][] pos2) {
        try {
            GifMaker gifMaker = new GifMaker(ImageIO.read(new File(path + "0.png")).getType(), 60, true);
            BufferedImage avatarImage1 = convertCircular(getAvatarImage(m1.getAvatarUrl()));
            BufferedImage avatarImage2 = convertCircular(getAvatarImage(m2.getAvatarUrl()));
            for (int i = 0; i < pos1.length; i++) {
                File f = new File(path + i + ".png");
                BufferedImage sticker = ImageIO.read(f);

                int x1 = pos1[i][0];
                int y1 = pos1[i][1];
                int w1 = pos1[i][2];
                int h1 = pos1[i][3];

                int x2 = pos2[i][0];
                int y2 = pos2[i][1];
                int w2 = pos2[i][2];
                int h2 = pos2[i][3];

                BufferedImage output = new BufferedImage(sticker.getWidth(), sticker.getHeight(), sticker.getType());
                Graphics2D g2d = output.createGraphics();
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, sticker.getWidth(), sticker.getHeight());
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0F));
                g2d.drawImage(sticker, 0, 0, sticker.getWidth(), sticker.getHeight(), null);
                g2d.drawImage(avatarImage1, x1, y1, w1, h1, null);
                g2d.drawImage(avatarImage2, x2, y2, w2, h2, null);
                g2d.dispose();
                gifMaker.writeToSequence(output);
            }

            gifMaker.close();
            ExternalResource resource = ExternalResource.create(gifMaker.getOutput());

            Image image = m1.uploadImage(resource);
            resource.close();
            return image;
        } catch (IOException ex) {
            System.out.println("(sendImage)构造GIF失败，请检查 PetData.java");
            ex.printStackTrace();
        }
        return null;
    }

    public static BufferedImage convertCircular(BufferedImage input) throws IOException {
        BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Ellipse2D.Double shape = new Ellipse2D.Double(0, 0, input.getWidth(), input.getHeight());
        Graphics2D g2 = output.createGraphics();
        g2.setClip(shape);

        if (Petpet.antialias){
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
            System.out.println("获取头像失败\nHttpURLConnection: "+conn+"\nURL: "+URL);
            e.printStackTrace();
        } finally {
            assert conn != null;
            conn.disconnect();
        }
        return image;
    }
}
