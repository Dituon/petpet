package xmmt.dituon.share;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class BaseImageMaker {
    // 单头像生成图片
    public InputStream makeOneAvatarImage(BufferedImage avatarImage, String path, int[] pos,
                                        boolean isAvatarOnTop, boolean isRotate, boolean isRound, boolean antialias) {
        try {
            BufferedImage sticker = ImageIO.read(new File(path + "0.png"));
            if (isRound) {
                avatarImage = ImageSynthesis.convertCircular(avatarImage, antialias);
            }
            return bufferedImageToInputStream(ImageSynthesis.synthesisImage(avatarImage, sticker, pos, isAvatarOnTop));
        } catch (IOException ex) {
            System.out.println("构造图片失败，请检查 PetData");
            ex.printStackTrace();
        }
        return null;
    }

    // 双头像生成图片
    public InputStream makeTwoAvatarImage(BufferedImage avatarImage1, BufferedImage avatarImage2, String path, int[] pos1, int[] pos2,
                                                 boolean isAvatarOnTop, boolean isRotate, boolean isRound, boolean antialias) {
        try {
            GifBuilder gifBuilder = new GifBuilder(ImageIO.read(new File(path + "0.png")).getType(), 60, true);

            if (isRound) {
                avatarImage1 = ImageSynthesis.convertCircular(avatarImage1, antialias);
                avatarImage2 = ImageSynthesis.convertCircular(avatarImage2, antialias);
            }

            BufferedImage sticker = ImageIO.read(new File(path + "0.png"));

            return bufferedImageToInputStream(ImageSynthesis.synthesisImage(
                    sticker, avatarImage1, avatarImage2, pos1, pos2, isAvatarOnTop));
        } catch (IOException ex) {
            System.out.println("构造图片失败，请检查 PetData");
            ex.printStackTrace();
        }
        return null;
    }

    private InputStream bufferedImageToInputStream(BufferedImage bf) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bf, "png", os);
        return new ByteArrayInputStream(os.toByteArray());
    }
}
