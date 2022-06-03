package xmmt.dituon.share;

import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;
import kotlinx.serialization.json.JsonObject;

import java.awt.*;
import java.util.Objects;

public class Text {
    private String text = null;
    private int[] pos = {2, 14};
    private Color color = new Color(25, 25, 25, 255); // #191919
    private Font font = null;

    public Text(JsonObject jsonObject, String[] info) {
        text = Objects.requireNonNull(jsonObject.get("text")).toString().replace("\"","")
                .replace("$from",info[0]).replace("$to",info[1]).replace("$group",info[2]);
        pos = jsonObject.containsKey("pos") ? setPos(jsonObject.get("pos")) : pos;
        color = jsonObject.containsKey("color") ? setColor(jsonObject.get("color")) : color;
        font = new Font(
                jsonObject.containsKey("font") ?
                        Objects.requireNonNull(jsonObject.get("font")).toString().replace("\"","") : "黑体",
                Font.PLAIN,
                jsonObject.containsKey("size") ?
                        Integer.parseInt(Objects.requireNonNull(jsonObject.get("size")).toString()) : 12
        );
    }

    private int[] setPos(JsonElement jsonElement) {
        JsonArray jsonArray = (JsonArray) jsonElement;
        int x = Integer.parseInt(jsonArray.get(0).toString());
        int y = Integer.parseInt(jsonArray.get(1).toString());
        return new int[]{x, y};
    }

    private Color setColor(JsonElement jsonElement) {
        int[] rgba = {25, 25, 25, 255};
        try { //rgb or rgba
            JsonArray jsonArray = (JsonArray) jsonElement;
            if (jsonArray.getSize() == 3 && jsonArray.getSize() == 4) {
                rgba[0] = Integer.parseInt(jsonArray.get(0).toString());
                rgba[1] = Integer.parseInt(jsonArray.get(1).toString());
                rgba[2] = Integer.parseInt(jsonArray.get(2).toString());
                rgba[3] = jsonArray.getSize() == 4 ? Integer.parseInt(jsonArray.get(3).toString()) : 255;
            }
        } catch (Exception ignored){ //hex
            String hex = jsonElement.toString().replace("#", "").replace("\"","");
            if (hex.length() != 6 && hex.length() != 8) {
                System.out.println("颜色格式有误，请输入正确的16进制颜色\n输入: " + hex);
                return color;
            }
            rgba[0] = Integer.parseInt(hex.substring(0, 2), 16);
            rgba[1] = Integer.parseInt(hex.substring(2, 4), 16);
            rgba[2] = Integer.parseInt(hex.substring(4, 6), 16);
            rgba[3] = hex.length() == 8 ? Integer.valueOf(hex.substring(6, 8), 16) : 255;
        }
        return new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    public String getText() {
        return text;
    }

    public int[] getPos() {
        return pos;
    }

    public Color getColor() {
        return color;
    }

    public Font getFont() {
        return font;
    }
}
