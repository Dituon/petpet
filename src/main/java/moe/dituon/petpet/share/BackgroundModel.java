package moe.dituon.petpet.share;

import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class BackgroundModel {
    public static final String DEFAULT_COLOR_STR = "#ffffff";
    public static final Color DEFAULT_COLOR = Color.decode(DEFAULT_COLOR_STR);
    private final int[] size;
    private BufferedImage image = null;
    private final Color color;
    private short length = 1;

    public BackgroundModel(BackgroundData data, List<AvatarModel> avatarList, List<TextModel> textList) {
        this(data, avatarList, textList, null);
    }

    public BackgroundModel(
            BackgroundData data,
            List<AvatarModel> avatarList,
            List<TextModel> textList,
            BufferedImage image
    ) {
        this.size = JsonArrayToIntArray(data.getSize(), avatarList, textList);
        this.image = image;
        this.color = data.getAwtColor();
        this.length = data.getLength() == 0 ?
                (avatarList.size() == 0 ? length : avatarList.get(0).getPosLength())
                : data.getLength();
    }

    public BufferedImage[] getImages(){
        var arr = new BufferedImage[this.length];
        Arrays.fill(arr, getImage());
        return arr;
    }

    public BufferedImage getImage() {
        BufferedImage output = new BufferedImage(size[0], size[1], 1);
        Graphics2D g2d = output.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, size[0], size[1]);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0F));
        if (image != null) g2d.drawImage(image, 0, 0, null);
        return output;
    }

    private int[] JsonArrayToIntArray(JsonArray ja, List<AvatarModel> avatarList, List<TextModel> textList) {
        int[] result = new int[ja.size()];
        short i = 0;
        for (JsonElement je : ja) {
            String str = je.toString().replace("\"", "");
            try {
                result[i] = Integer.parseInt(str);
            } catch (NumberFormatException ignored) {
                ArithmeticParser parser = new ArithmeticParser(str);
                for (short in = 0; in < avatarList.size(); in++) {
                    parser.put("avatar" + in + "Width", avatarList.get(in).getImageWidth());
                    parser.put("avatar" + in + "Height", avatarList.get(in).getImageHeight());
                }
                for (short in = 0; in < textList.size(); in++) {
                    parser.put("text" + in + "Width", textList.get(in).getWidth(textList.get(in).getFont()));
                    parser.put("text" + in + "Height", textList.get(in).getHeight(textList.get(in).getFont()));
                }
                result[i] = (int) parser.eval();
            }
            i++;
        }
        return result;
    }
}
