package xmmt.dituon.share;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class BaseGifMaker {

    public InputStream makeOneAvatarGIF(BufferedImage avatarImage, String path, int[][] pos,
                                      boolean isAvatarOnTop, boolean isRotate, boolean isRound, boolean antialias) {
        int i = 0;
        try {
            GifBuilder gifBuilder = new GifBuilder(ImageIO.read(new File(path + "0.png")).getType(), 65, true);
            if (isRound) {
                avatarImage = ImageSynthesis.convertCircular(avatarImage, antialias);
            }
            for (int[] p : pos) {
                File f = new File(path + i + ".png");
                i++;
                BufferedImage sticker = ImageIO.read(f);
                if (isRotate) {
                    gifBuilder.writeToSequence(ImageSynthesis.synthesisImage(avatarImage, sticker, p, i, isAvatarOnTop));
                    continue;
                }
                gifBuilder.writeToSequence(ImageSynthesis.synthesisImage(avatarImage, sticker, p, isAvatarOnTop));
            }
            gifBuilder.close();
            return gifBuilder.getOutput();
        } catch (IOException ex) {
            System.out.println("构造GIF失败，请检查 PetData.java");
            ex.printStackTrace();
        }
        return null;
    }

    public InputStream makeTwoAvatarGIF(BufferedImage avatarImage1, BufferedImage avatarImage2, String path, int[][] pos1, int[][] pos2,
                                               boolean isAvatarOnTop, boolean isRotate, boolean isRound, boolean antialias) {
        try {
            GifBuilder gifBuilder = new GifBuilder(ImageIO.read(new File(path + "0.png")).getType(), 65, true);
            if (isRound) {
                avatarImage1 = ImageSynthesis.convertCircular(avatarImage1, antialias);
                avatarImage2 = ImageSynthesis.convertCircular(avatarImage2, antialias);
            }
            for (int i = 0; i < pos1.length; i++) {
                File f = new File(path + i + ".png");
                BufferedImage sticker = ImageIO.read(f);

                if (isRotate) {
                    gifBuilder.writeToSequence(ImageSynthesis.synthesisImage(
                            sticker, avatarImage1, avatarImage2, pos1[i], pos2[i], i+1, isAvatarOnTop));
                    continue;
                }
                gifBuilder.writeToSequence(ImageSynthesis.synthesisImage(
                        sticker, avatarImage1, avatarImage2, pos1[i], pos2[i], isAvatarOnTop));
            }
            gifBuilder.close();
            return gifBuilder.getOutput();
        } catch (IOException ex) {
            System.out.println("构造GIF失败，请检查 PetData.java");
            ex.printStackTrace();
        }
        return null;
    }

}
