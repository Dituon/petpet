package xmmt.dituon.share;

import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextModel {
    protected String text;
    protected int[] pos = {2, 14};
    protected Color color = new Color(25, 25, 25, 255); // #191919
    protected Font font;
    protected TextAlign align;
    protected TextWrap wrap;
    private static Graphics2D container = null;
    private int width = 0;
    private int height = 0;
    private short line = 1;

    public TextModel(TextData textData, TextExtraData extraInfo) {
        text = extraInfo != null ? buildText(textData.getText(), extraInfo)
                : textData.getText().replace("\"", "");
        pos = textData.getPos() != null ? setPos(textData.getPos()) : pos;
        color = textData.getColor() != null ? setColor(textData.getColor()) : color;
        font = loadFont(textData.getFont() != null ? textData.getFont().replace("\"", "") : "黑体",
                textData.getSize() != null ? textData.getSize() : 12);
        align = textData.getAlign();
        wrap = textData.getWrap();
    }

    private String buildText(String text, TextExtraData extraData) {
        text = text.replace("\"", "")
                .replace("$from", extraData.getFromReplacement())
                .replace("$to", extraData.getToReplacement())
                .replace("$group", extraData.getGroupReplacement())
                .replace("\\n","\n")
                .replace("\\s"," ");

        String regex = "\\$txt([1-9])\\[(.*)]"; //$txt(num)[(xxx)]
        Matcher m = Pattern.compile(regex).matcher(text);
        while (m.find()) {
            short i = Short.parseShort(m.group(1));
            String replaceText = m.group(2);
            try {
                replaceText = extraData.getTextList().get(i - 1);
            } catch (IndexOutOfBoundsException ignored) {
            }
            text = text.replaceAll(regex, replaceText);
        }
        for (char t : text.toCharArray()) {
            if (t == '\n') line += 1;
        }
        return text;
    }

    private static Font loadFont(String fontName, int size) {
        return new Font(fontName, Font.PLAIN, size);
    }

    private int[] setPos(List<Integer> posElements) {
        int x = posElements.get(0);
        int y = posElements.get(1);
        int w = posElements.size() == 3 ? posElements.get(2) : 200;
        return new int[]{x, y, w};
    }

    private Color setColor(JsonElement jsonElement) {
        short[] rgba = {25, 25, 25, 255};
        try { //rgb or rgba
            JsonArray jsonArray = (JsonArray) jsonElement;
            if (jsonArray.getSize() == 3 || jsonArray.getSize() == 4) {
                rgba[0] = Short.parseShort(jsonArray.get(0).toString());
                rgba[1] = Short.parseShort(jsonArray.get(1).toString());
                rgba[2] = Short.parseShort(jsonArray.get(2).toString());
                rgba[3] = jsonArray.getSize() == 4 ? Short.parseShort(jsonArray.get(3).toString()) : 255;
            }
        } catch (Exception ignored) { //hex
            String hex = jsonElement.toString().replace("#", "").replace("\"", "");
            if (hex.length() != 6 && hex.length() != 8) {
                System.out.println("颜色格式有误，请输入正确的16进制颜色\n输入: " + hex);
                return color;
            }
            rgba[0] = Short.parseShort(hex.substring(0, 2), 16);
            rgba[1] = Short.parseShort(hex.substring(2, 4), 16);
            rgba[2] = Short.parseShort(hex.substring(4, 6), 16);
            rgba[3] = hex.length() == 8 ? Short.parseShort(hex.substring(6, 8), 16) : 255;
        }
        return new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    /**
     * 获取构建后的文本数据
     */
    public String getText() {
        if (wrap == TextWrap.BREAK && pos.length >= 3) {
            int width = this.getWidth(font);
            if (width <= pos[2]) return text;

            short lineAp = (short) (width / pos[2]);
            StringBuilder builder = new StringBuilder(text);
            short lineWidth = (short) (text.length() / lineAp);
            short i = 1;
            while (i <= lineAp) {
                builder.insert(lineWidth * i++, '\n');
            }

            return builder.toString();
        }
        return text;
    }

    /**
     * 获取构建后的坐标
     *
     * @return int[2]{x, y}
     */
    public int[] getPos() {
        switch (align) {
            case CENTER:
                return new int[]{pos[0] - this.getWidth(this.getFont()) / 2, pos[1]};
            case RIGHT:
                return new int[]{pos[0] - this.getWidth(this.getFont()), pos[1]};
        }
        return pos;
    }

    /**
     * 获取颜色 (默认为 #191919)
     */
    public Color getColor() {
        return color;
    }

    /**
     * 获取构建后的字体
     */
    public Font getFont() {
        if (wrap == TextWrap.ZOOM) {
            float multiple = Math.min(1.0F, (float) pos[2] / this.getWidth(font));
            return new Font(font.getFontName(), Font.PLAIN, Math.round(font.getSize() * multiple));
        }
        return font;
    }

    /**
     * 获取文字渲染后的宽度 (包含 \n)
     *
     * @param font 渲染字体
     */
    public int getWidth(Font font) {
        if (width == 0) for (String p : this.getText().split("\n")) {
            width = Math.max(width, getTextWidth(p, font));
        }
        return width;
    }

    /**
     * 获取字体渲染后的高度 (包含 \n)
     *
     * @param font 渲染字体
     */
    public int getHeight(Font font) {
        if (height == 0) height = getFontHeight(font);
        return height * line;
    }

    /**
     * 获取文字渲染后的宽度 (不渲染 \n)
     *
     * @param font 渲染字体
     */
    public static int getTextWidth(String text, Font font) {
        if (container == null) container = new BufferedImage(1, 1, 1).createGraphics();
        return container.getFontMetrics(font).stringWidth(text);
    }

    /**
     * 获取字体渲染后的高度 (不渲染 \n)
     *
     * @param font 渲染字体
     */
    public static int getFontHeight(Font font) {
        if (container == null) container = new BufferedImage(1, 1, 1).createGraphics();
        return container.getFontMetrics(font).getHeight();
    }
}
