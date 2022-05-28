package xmmt.dituon;

import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import xmmt.dituon.share.BaseGifMaker;
import xmmt.dituon.share.GifBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static xmmt.dituon.share.ImageSynthesis.*;

public class GifMaker extends BaseGifMaker {
    // 单头像GIF
    public static Image makeGIF(Member m, String path, int[][] pos, boolean isAvatarOnTop, boolean isRotate, boolean isRound) {
        return makeGIF(m, m.getAvatarUrl(), path, pos, isAvatarOnTop, isRotate, isRound);
    }

    public static Image makeGIF(Member m, String URL, String path, int[][] pos,
                                boolean isAvatarOnTop, boolean isRotate, boolean isRound) {
        BufferedImage avatarImage = getAvatarImage(URL);
        InputStream gifResultStream = makeOneAvatarGIF(avatarImage, path, pos, isAvatarOnTop, isRotate, isRound);
        try {
            if (gifResultStream != null) {
                ExternalResource resource = ExternalResource.create(gifResultStream);

                Image image = m.uploadImage(resource);
                resource.close();
                return image;
            }
        } catch (IOException ex) {
            System.out.println("构造ExternalResource失败");
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
        BufferedImage avatarImage1 = getAvatarImage(m1URL);
        BufferedImage avatarImage2 = getAvatarImage(m2URL);
        try {
            GifBuilder gifBuilder = new GifBuilder(ImageIO.read(new File(path + "0.png")).getType(), 60, true);


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
                    continue;
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
