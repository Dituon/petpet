package xmmt.dituon.share;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BaseImageMaker {
    public static InputStream makeImage(ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
                                        BufferedImage sticker, boolean antialias, Encoder encoder) {
        return makeImage(avatarList, textList, sticker, antialias, null, encoder);
    }

    public static InputStream makeImage(
            ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
            BufferedImage sticker, boolean antialias, List<Integer> maxSize,
            Encoder encoder) {
        for (AvatarModel avatar : avatarList) {
            if (avatar.isGif()) return BaseGifMaker.makeGIF(
                    avatarList, textList, sticker, antialias, maxSize, encoder, 65);
        }
        try {
            return bufferedImageToInputStream(ImageSynthesis.synthesisImage(
                    sticker, avatarList, textList, antialias, true));
        } catch (IOException e) {
            System.out.println("构造IMG失败，请检查 PetData");
            e.printStackTrace();
        }
        return null;
    }

    private static InputStream bufferedImageToInputStream(BufferedImage bf) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bf, "png", os);
        return new ByteArrayInputStream(os.toByteArray());
    }
}
