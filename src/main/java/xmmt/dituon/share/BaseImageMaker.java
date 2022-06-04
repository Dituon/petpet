package xmmt.dituon.share;

import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class BaseImageMaker {
    // 无头像生成图片
    public InputStream makeNoneAvatarImage(String path,
                                          ArrayList<Text> texts) {
        return makeImageGeneral(null, null, path, null, null, false, false, false, texts);
    }

    // 单头像生成图片
    public InputStream makeOneAvatarImage(BufferedImage avatarImage, String path, int[] pos,
                                          boolean isAvatarOnTop, boolean isRound, boolean antialias,
                                          ArrayList<Text> texts) {
        return makeImageGeneral(avatarImage, null, path, pos, null, isAvatarOnTop, isRound, antialias, texts);
    }

    // 双头像生成图片
    public InputStream makeTwoAvatarImage(BufferedImage avatarImage1, BufferedImage avatarImage2, String path,
                                          int[] pos1, int[] pos2,boolean isAvatarOnTop,
                                          boolean isRound, boolean antialias, ArrayList<Text> texts) {
        return makeImageGeneral(avatarImage1, avatarImage2, path, pos1, pos2, isAvatarOnTop, isRound, antialias, texts);
    }

    private InputStream makeImageGeneral(@Nullable BufferedImage avatarImage1,
                                         @Nullable BufferedImage avatarImage2,
                                         String path,
                                         @Nullable int[] pos1, @Nullable int[] pos2,
                                         boolean isAvatarOnTop,
                                         boolean isRound,
                                         boolean antialias,
                                         @Nullable ArrayList<Text> texts) {
        try {
            if (isRound) {
                avatarImage1 = avatarImage1 != null ? ImageSynthesis.convertCircular(avatarImage1, antialias) : null;
                avatarImage2 = avatarImage2 != null ? ImageSynthesis.convertCircular(avatarImage2, antialias) : null;
            }

            BufferedImage sticker = ImageIO.read(new File(path + "0.png"));

            return bufferedImageToInputStream(ImageSynthesis.synthesisImage(
                    sticker, avatarImage1, avatarImage2, pos1, pos2, isAvatarOnTop, texts));
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
