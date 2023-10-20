package moe.dituon.petpet.share;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextModel {
    public static final byte LINE_SPACING = 2;
    public static final String DEFAULT_COLOR_STR = "#191919";
    public static final Color DEFAULT_COLOR = Color.decode(DEFAULT_COLOR_STR);
    public static final String DEFAULT_STROKE_COLOR_STR = "#fffff";
    public static final Color DEFAULT_STROKE_COLOR = Color.decode(DEFAULT_STROKE_COLOR_STR);
    public static final String DEFAULT_FONT = "SimHei";
    public static final Pattern TEXT_VAR_REGEX = Pattern.compile("\\$txt([1-9]\\d*)\\[(.*?)]"); //$txt(num)[(xxx)]

    protected String text;
    protected int[] pos;
    protected Color color;
    protected Font font;
    protected TextAlign align;
    protected TextWrap wrap;
    protected List<Position> position;
    protected short strokeSize;
    protected Color strokeColor;
    private static Graphics2D container = null;
    private short line = 1;

    public TextModel(TextData textData, TextExtraData extraInfo) {
        text = extraInfo != null ? buildText(
                textData.getText(), extraInfo, textData.getGreedy()
        ) : textData.getText();
        pos = setPos(textData.getPos());
        color = textData.getAwtColor();
        font = loadFont(textData.getFont(), textData.getSize(), textData.getStyle());
        align = textData.getAlign();
        wrap = textData.getWrap();
        position = textData.getPosition();
        if (position == null || position.size() != 2) position = null;
        strokeSize = textData.getStrokeSize();
        strokeColor = textData.getStrokeAwtColor();
    }

    private String buildText(String text, TextExtraData extraData, boolean greedy) {
        text = text.replace("$from", extraData.getFromReplacement())
                .replace("$to", extraData.getToReplacement())
                .replace("$group", extraData.getGroupReplacement());

        Matcher m = TEXT_VAR_REGEX.matcher(text);
        if (greedy) {
            List<String> textList = new ArrayList<>(extraData.getTextList());
            short maxIndex = 0;
            while (m.find()) maxIndex++;
            m.reset();
            for (short index = 0; m.find(); index++) {
                short i = Short.parseShort(m.group(1));
                String replaceText = i > textList.size() ?
                        m.group(2) : (index == maxIndex ?
                        textList.remove(i - 1) : String.join(" ", textList)
                );
                text = text.replace(m.group(0), replaceText);
            }
        } else {
            while (m.find()) {
                short i = Short.parseShort(m.group(1));
                String replaceText = i > extraData.getTextList().size() ?
                        m.group(2) : extraData.getTextList().get(i - 1);
                text = text.replace(m.group(0), replaceText);
            }
        }
        char[] chars = text.toCharArray();
        for (char t : chars) {
            if (t == '\n' && chars[chars.length - 1] != '\n') line += 1;
        }
        return text;
    }

    private static Font loadFont(String fontName, int size, TextStyle style) {
        return new Font(fontName, style.getValue(), size);
    }

    private int[] setPos(int[] posArr) {
        int x = posArr[0];
        int y = posArr[1];
        int w = posArr.length >= 3 ? posArr[2] : 200;
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
     * 获取构建后的坐标(返回新对象)
     *
     * @return int[2]{x, y}
     */
    public int[] getPos() {
        switch (align) {
            case CENTER:
                return new int[]{
                        pos[0] - this.getWidth(this.getFont()) / 2,
                        line == 1 ? pos[1] + getTextHeight(text, getFont()) / 2
                                : pos[1] - (getHeight(getFont()) / 2)
                                + (getTextHeight(text, getFont()) / 2) + LINE_SPACING
                };
            case RIGHT:
                return new int[]{pos[0] - this.getWidth(this.getFont()), pos[1]};
        }
        return pos.clone();
    }

    /**
     * 获取颜色 (默认为 #191919)
     */
    public Color getColor() {
        return color;
    }

    public void zoomFont(float multiple) {
        font = new Font(font.getFontName(), font.getStyle(), Math.round(font.getSize() * multiple));
    }

    public List<Position> getPosition() {
        return position;
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
     * 在Graphics2D对象上绘制TextModel
     *
     * @param g2d           画布
     * @param stickerWidth  画布宽度, 用于计算坐标
     * @param stickerHeight 画布高度, 用于计算坐标
     */
    public void drawAsG2d(Graphics2D g2d, int stickerWidth, int stickerHeight) {
        if (position == null) {
            ImageSynthesisCore.g2dDrawText(g2d, getText(), getPos(), this.color, getFont());
            return;
        }
        int[] pos = getPos();
        switch (position.get(0)) {
            case RIGHT:
                pos[0] = stickerWidth - pos[0];
                break;
            case CENTER:
                pos[0] = stickerWidth / 2 + pos[0];
                break;
        }
        switch (position.get(1)) {
            case BOTTOM:
                pos[1] = stickerHeight - pos[1];
                break;
            case CENTER:
                pos[1] = stickerHeight / 2 + pos[1];
                break;
        }

        if (strokeSize != 0) {
            ImageSynthesisCore.g2dDrawStrokeText(
                    g2d, getText(), pos, this.color, getFont(), strokeSize, strokeColor);
            return;
        }

        ImageSynthesisCore.g2dDrawText(g2d, getText(), pos, this.color, getFont());
    }

    /**
     * 获取文字渲染后的宽度 (包含 \n)
     *
     * @param font 渲染字体
     */
    public int getWidth(Font font) {
        int width = 0;
        for (String p : this.text.split("\n")) {
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
        return line == 1 ? getTextHeight(text, font) :
                (getTextHeight(text, font) + LINE_SPACING) * line;
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
        FontMetrics fm = container.getFontMetrics(font);
        return fm.getAscent() - fm.getDescent() - fm.getLeading();
    }
}
