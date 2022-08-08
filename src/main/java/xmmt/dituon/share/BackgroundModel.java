package xmmt.dituon.share;

import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class BackgroundModel {
    private final int[] size;
    private BufferedImage image = null;
    private final Color color;

    public BackgroundModel(BackgroundData data, ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList) {
        this(data, avatarList, textList, null);
    }

    public BackgroundModel(BackgroundData data,
                           ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList, BufferedImage image) {
        this.size = JsonArrayToIntArray(data.getSize(), avatarList, textList);
        this.image = image;
        this.color = BasePetService.decodeColor(data.getColor(), new short[]{255, 255, 255, 255});
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

    private int[] JsonArrayToIntArray(JsonArray ja, ArrayList<AvatarModel> avatarList, ArrayList<TextModel> textList) {
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
