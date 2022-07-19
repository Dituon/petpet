package xmmt.dituon.share;

import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class BackgroundModel {
    private final int[] size;

    public BackgroundModel(BackgroundData data, ArrayList<AvatarModel> avatarList) {
        size = JsonArrayToIntArray(data.getSize(), avatarList);
    }

    public BufferedImage getImage() {
        BufferedImage output = new BufferedImage(size[0], size[1], 1);
        Graphics2D g2d = output.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, size[0], size[1]);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0F));
        return output;
    }

    private int[] JsonArrayToIntArray(JsonArray ja, ArrayList<AvatarModel> avatarList) {
        int[] result = new int[ja.size()];
        short i = 0;
        for (JsonElement je : ja) {
            String str = je.toString().replace("\"", "");
            try {
                result[i] = Integer.parseInt(str);
            } catch (NumberFormatException ignored) {
                short avatarSize = (short) avatarList.size();
                ArithmeticParser parser = new ArithmeticParser(str);
                for (short in = 0; in < avatarSize; in++) {
                    parser.put("avatar" + in + "Width", avatarList.get(in).getImageWidth());
                    parser.put("avatar" + in + "Height", avatarList.get(in).getImageHeight());
                }
                result[i] = (int) parser.eval();
            }
            i++;
        }
        return result;
    }
}
