package xmmt.dituon.share;

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
    private short line = 1;

    public TextModel(TextData textData, TextExtraData extraInfo) {
        text = extraInfo != null ? buildText(textData.getText(), extraInfo)
                : textData.getText().replace("\"", "");
        pos = textData.getPos() != null ? setPos(textData.getPos()) : pos;
        color = textData.getColor() != null ?
                BasePetService.decodeColor(textData.getColor(), new short[]{25, 25, 25, 255}) : color;
        font = loadFont(textData.getFont() != null ? textData.getFont().replace("\"", "") : "黑体",
                textData.getSize() != null ? textData.getSize() : 12,
                textData.getStyle() != null ? textData.getStyle() : TextStyle.PLAIN);
        align = textData.getAlign();
        wrap = textData.getWrap();
    }

    private String buildText(String text, TextExtraData extraData) {
        text = text.replace("\"", "")
                .replace("$from", extraData.getFromReplacement())
                .replace("$to", extraData.getToReplacement())
                .replace("$group", extraData.getGroupReplacement())
                .replace("\\n", "\n")
                .replace("\\s", " ");

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

    private static Font loadFont(String fontName, int size, TextStyle style) {
        return new Font(fontName, style.ordinal(), size);
    }

    private int[] setPos(List<Integer> posElements) {
        int x = posElements.get(0);
        int y = posElements.get(1);
        int w = posElements.size() == 3 ? posElements.get(2) : 200;
        return new int[]{x, y, w};
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
                return new int[]{pos[0] - this.getWidth(this.getFont()) / 2,
                        pos[1] + this.getHeight(this.getFont()) / 2};
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
            return new Font(font.getFontName(), font.getStyle(), Math.round(font.getSize() * multiple));
        }
        return font;
    }

    /**
     * 获取文字渲染后的宽度 (包含 \n)
     *
     * @param font 渲染字体
     */
    public int getWidth(Font font) {
        int width = 0;
        for (String p : this.getText().split("\n")) {
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
        return getTextHeight(text, font) * line;
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
    public static int getTextHeight(String text, Font font) {
        if (container == null) container = new BufferedImage(1, 1, 1).createGraphics();
        return (int) font.createGlyphVector(
                container.getFontMetrics(font).getFontRenderContext(), text).getVisualBounds().getHeight();
    }
}
