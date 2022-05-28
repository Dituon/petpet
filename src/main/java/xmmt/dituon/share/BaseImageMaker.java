package xmmt.dituon.share;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

import static xmmt.dituon.share.ImageSynthesis.*;

public class BaseImageMaker {
    // 单头像生成图片
    public static InputStream makeOneAvatarImage(BufferedImage avatarImage, String path, int[] pos,
                                        boolean isAvatarOnTop, boolean isRotate, boolean isRound) {
        try {
            BufferedImage sticker = ImageIO.read(new File(path + "0.png"));
            if (isRound) {
                avatarImage = convertCircular(avatarImage);
            }
            return bufferedImageToInputStream(synthesisImage(avatarImage, sticker, pos, isAvatarOnTop));
        } catch (IOException ex) {
            System.out.println("构造图片失败，请检查 PetData");
            ex.printStackTrace();
        }
        return null;
    }

    // 双头像生成图片
    public static InputStream makeTwoAvatarImage(BufferedImage avatarImage1, BufferedImage avatarImage2, String path, int[] pos1, int[] pos2,
                                                 boolean isAvatarOnTop, boolean isRotate, boolean isRound) {
        try {
            GifBuilder gifBuilder = new GifBuilder(ImageIO.read(new File(path + "0.png")).getType(), 60, true);

            if (isRound) {
                avatarImage1 = convertCircular(avatarImage1);
                avatarImage2 = convertCircular(avatarImage2);
            }

            BufferedImage sticker = ImageIO.read(new File(path + "0.png"));

            return bufferedImageToInputStream(synthesisImage(
                    sticker, avatarImage1, avatarImage2, pos1, pos2, isAvatarOnTop));
        } catch (IOException ex) {
            System.out.println("构造图片失败，请检查 PetData");
            ex.printStackTrace();
        }
        return null;
    }

    private static InputStream bufferedImageToInputStream(BufferedImage bf) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bf, "png", os);
        return new ByteArrayInputStream(os.toByteArray());
    }
}
