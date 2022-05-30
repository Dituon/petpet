package xmmt.dituon;

import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static xmmt.dituon.ImageSynthesis.*;

public class GifMaker {
    // 单头像GIF
    public static Image makeGIF(Member m, String path, int[][] pos, boolean isAvatarOnTop, boolean isRotate, boolean isRound) {
        return makeGIF(m, m.getAvatarUrl(), path, pos, isAvatarOnTop, isRotate, isRound);
    }

    public static Image makeGIF(Member m, String URL, String path, int[][] pos,
                                boolean isAvatarOnTop, boolean isRotate, boolean isRound) {
        int i = 0;
        try {
            GifBuilder gifBuilder = new GifBuilder(ImageIO.read(new File(path + "0.png")).getType(), 65, true);
            BufferedImage avatarImage = getAvatarImage(URL);
            if (isRound) {
                avatarImage = convertCircular(avatarImage);
            }
            for (int[] p : pos) {
                File f = new File(path + i + ".png");
                i++;
                BufferedImage sticker = ImageIO.read(f);
                if (isRotate) {
                    gifBuilder.writeToSequence(synthesisImage(avatarImage, sticker, p, i, isAvatarOnTop));
                    break;
                }
                gifBuilder.writeToSequence(synthesisImage(avatarImage, sticker, p, isAvatarOnTop));
            }
            gifBuilder.close();
            ExternalResource resource = ExternalResource.create(gifBuilder.getOutput());

            Image image = m.uploadImage(resource);
            resource.close();
            return image;
        } catch (IOException ex) {
            System.out.println("构造GIF失败，请检查 PetData.java");
            ex.printStackTrace();
        }
        return null;
    }

    // 两张头像GIF
    public static Image makeGIF(Member m1, Member m2, String path, int[][] pos1, int[][] pos2,
                                boolean isAvatarOnTop, boolean isRotate, boolean isRound) {
        return makeGIF(m1, m1.getAvatarUrl(), m2.getAvatarUrl(), path, pos1, pos2, isAvatarOnTop, isRotate, isRound);
    }

    public static Image makeGIF(Member m, String m1URL, String m2URL, String path, int[][] pos1, int[][] pos2,
                                boolean isAvatarOnTop, boolean isRotate, boolean isRound) {
        try {
            GifBuilder gifBuilder = new GifBuilder(ImageIO.read(new File(path + "0.png")).getType(), 60, true);
            BufferedImage avatarImage1 = getAvatarImage(m1URL);
            BufferedImage avatarImage2 = getAvatarImage(m2URL);

            if (isRound) {
                avatarImage1 = convertCircular(avatarImage1);
                avatarImage2 = convertCircular(avatarImage2);
            }

            for (int i = 0; i < pos1.length; i++) {
                File f = new File(path + i + ".png");
                BufferedImage sticker = ImageIO.read(f);

                if (isRotate) {
                    gifBuilder.writeToSequence(synthesisImage(
                            sticker, avatarImage1, avatarImage2, pos1[i], pos2[i], i+1, isAvatarOnTop));
                    break;
                }
                gifBuilder.writeToSequence(synthesisImage(
                        sticker, avatarImage1, avatarImage2, pos1[i], pos2[i], isAvatarOnTop));
            }

            gifBuilder.close();
            ExternalResource resource = ExternalResource.create(gifBuilder.getOutput());

            Image image = m.uploadImage(resource);
            resource.close();
            return image;
        } catch (IOException ex) {
            System.out.println("构造GIF失败，请检查 PetData");
            ex.printStackTrace();
        }
        return null;
    }
}
