package xmmt.dituon.share;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class BaseGifMaker {

    public static InputStream makeGIF(ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
                                      HashMap<Short, BufferedImage> stickerMap, boolean antialias) {
        try {
            //遍历获取GIF长度(图片文件数量)
            GifBuilder gifBuilder = new GifBuilder(stickerMap.get((short) 0).getType(), 65, true);

            for (short key : stickerMap.keySet()) {
                gifBuilder.writeToSequence(ImageSynthesis.synthesisImage(
                        stickerMap.get(key), avatarList, textList, antialias, false));
            }
            gifBuilder.close();
            return gifBuilder.getOutput();
        } catch (IOException ex) {
            System.out.println("构造GIF失败，请检查 PetData");
            ex.printStackTrace();
        }
        return null;
    }

    public static InputStream makeGIF(ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
                                      BufferedImage sticker, boolean antialias) {
        try {
            GifBuilder gifBuilder = new GifBuilder(sticker.getType(), 65, true);

            short maxFrameLength = 1;
            for (AvatarModel avatar : avatarList) {
                maxFrameLength = (short) avatar.getImageList().size();
            }
            for (short i = 0; i < maxFrameLength; i++) {
                gifBuilder.writeToSequence(ImageSynthesis.synthesisImage(
                        sticker, avatarList, textList, antialias, false
                ));
            }
            gifBuilder.close();
            return gifBuilder.getOutput();
        } catch (IOException ex) {
            System.out.println("构造GIF失败，请检查 PetData");
            ex.printStackTrace();
        }
        return null;
    }
}
