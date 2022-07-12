package xmmt.dituon.share;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class BaseGifMaker {
    private final HashMap<String, Short> imageNumMap = new HashMap<>();

    public InputStream makeAvatarGIF(String path, ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList,
                                     boolean antialias) {
        try {
            //遍历获取GIF长度(图片文件数量)
            if(!imageNumMap.containsKey(path)){
                short imageNum = 0;
                for (File file : Objects.requireNonNull(new File(path).listFiles())) {
                    if (file.getName().endsWith(".png")) imageNum++;
                }
                imageNumMap.put(path, imageNum);
            }


            GifBuilder gifBuilder = new GifBuilder(ImageIO.read(new File(
                    path + "0.png")).getType(), 65, true);

            for (int i = 0; i < imageNumMap.get(path); i++) {
                BufferedImage sticker = ImageIO.read(new File(path + i + ".png"));
                gifBuilder.writeToSequence(ImageSynthesis.synthesisImage(sticker, avatarList, textList, antialias, false));
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
