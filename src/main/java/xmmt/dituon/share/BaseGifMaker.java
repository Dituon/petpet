package xmmt.dituon.share;

import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class BaseGifMaker {

    public InputStream makeOneAvatarGIF(BufferedImage avatarImage, String path, int[][] pos,
                                        boolean isAvatarOnTop, boolean isRotate, boolean isRound,
                                        boolean antialias, ArrayList<TextModel> texts) {
        return makeGifGeneral(avatarImage, null, path, pos, null, isAvatarOnTop, isRotate, isRound, antialias, texts);
    }

    public InputStream makeTwoAvatarGIF(BufferedImage avatarImage1, BufferedImage avatarImage2, String path,
                                        int[][] pos1, int[][] pos2, boolean isAvatarOnTop, boolean isRotate,
                                        boolean isRound, boolean antialias, ArrayList<TextModel> texts) {
        return makeGifGeneral(avatarImage1, avatarImage2, path, pos1, pos2, isAvatarOnTop, isRotate, isRound, antialias, texts);
    }

    public InputStream makeGifGeneral(BufferedImage avatarImage1, @Nullable BufferedImage avatarImage2,
                                      String path,
                                      int[][] pos1, @Nullable int[][] pos2,
                                      boolean isAvatarOnTop,
                                      boolean isRotate,
                                      boolean isRound,
                                      boolean antialias,
                                      @Nullable ArrayList<TextModel> texts) {
        try {
            GifBuilder gifBuilder = new GifBuilder(ImageIO.read(new File(path + "0.png")).getType(), 65, true);
            if (isRound) {
                avatarImage1 = avatarImage1 != null ? ImageSynthesis.convertCircular(avatarImage1, antialias) : null;
                avatarImage2 = avatarImage2 != null ? ImageSynthesis.convertCircular(avatarImage2, antialias) : null;
            }
            // TODO 若pos1变为@Nullable，需要别的方法判断length
            for (int i = 0; i < pos1.length; i++) {
                File f = new File(path + i + ".png");
                BufferedImage sticker = ImageIO.read(f);

                int rotateIndex = isRotate ? i + 1 : 0;
                int[] pos1OfThisFrame = pos1 != null ? pos1[i] : null;
                int[] pos2OfThisFrame = pos2 != null ? pos2[i] : null;
                gifBuilder.writeToSequence(ImageSynthesis.synthesisImage(
                            sticker, avatarImage1, avatarImage2, pos1OfThisFrame, pos2OfThisFrame, rotateIndex,
                        isAvatarOnTop,antialias, texts));
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
