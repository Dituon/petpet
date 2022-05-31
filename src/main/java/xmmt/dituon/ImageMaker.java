package xmmt.dituon;

import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import xmmt.dituon.share.BaseImageMaker;
import xmmt.dituon.share.GifBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

import static xmmt.dituon.share.ImageSynthesis.*;

public class ImageMaker extends BaseImageMaker {
    // 单头像生成图片
    public static Image makeImage(Member m, String path, int[] pos,
                                  boolean isAvatarOnTop, boolean isRotate, boolean isRound) {
        return makeImage(m, m.getAvatarUrl(), path, pos, isAvatarOnTop, isRotate, isRound);
    }

    public static Image makeImage(Member m, String URL, String path, int[] pos,
                                  boolean isAvatarOnTop, boolean isRotate, boolean isRound) {
        try {
            BufferedImage avatarImage = getAvatarImage(URL);
            ExternalResource res = ExternalResource.create(makeOneAvatarImage(avatarImage, path, pos, isAvatarOnTop, isRotate, isRound));
            Image img = m.uploadImage(res);
            res.close();
            return img;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // 双头像生成图片
    public static Image makeImage(Member m1, Member m2, String path, int[] pos1, int[] pos2,
                                  boolean isAvatarOnTop, boolean isRotate, boolean isRound) {
        return makeImage(m1, m1.getAvatarUrl(), m2.getAvatarUrl(), path, pos1, pos2, isAvatarOnTop, isRotate, isRound);
    }

    public static Image makeImage(Member m, String m1URL, String m2URL, String path, int[] pos1, int[] pos2,
                                  boolean isAvatarOnTop, boolean isRotate, boolean isRound) {
        try {
            GifBuilder gifBuilder = new GifBuilder(ImageIO.read(new File(path + "0.png")).getType(), 60, true);
            BufferedImage avatarImage1 = getAvatarImage(m1URL);
            BufferedImage avatarImage2 = getAvatarImage(m2URL);

            if (isRound) {
                avatarImage1 = convertCircular(avatarImage1);
                avatarImage2 = convertCircular(avatarImage2);
            }

            BufferedImage sticker = ImageIO.read(new File(path + "0.png"));

            gifBuilder.writeToSequence(synthesisImage(
                    sticker, avatarImage1, avatarImage2, pos1, pos2, isAvatarOnTop));

            gifBuilder.close();
            ExternalResource resource = ExternalResource.create(gifBuilder.getOutput());

            Image image = m.uploadImage(resource);
            resource.close();
            return image;
        } catch (IOException ex) {
            System.out.println("构造图片失败，请检查 PetData");
            ex.printStackTrace();
        }
        return null;
    }

    private static InputStream bufferedImageToInputStream(BufferedImage bf) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bf, "img", os);
        return new ByteArrayInputStream(os.toByteArray());
    }
}
