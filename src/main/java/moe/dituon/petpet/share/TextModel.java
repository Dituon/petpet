package moe.dituon.petpet.share;

import java.awt.*;
import java.awt.geom.AffineTransform;
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
    protected short angle;
    protected Color color;
    protected Font font;
    protected TextAlign align;
    protected TextWrap wrap;
    protected List<Position> position;
    protected TransformOrigin transformOrigin;
    protected short strokeSize;
    protected Color strokeColor;
    private static Graphics2D container = null;
    private short line = 1;
    private boolean zoomed = false;
    private int x;
    private int y;
    private int width;
    private int height;

    public TextModel(TextData textData, TextExtraData extraInfo) {
        text = extraInfo != null ? buildText(
                textData.getText(), extraInfo, textData.getGreedy()
        ) : textData.getText();
        pos = textData.getPos();
        angle = textData.getAngle();
        color = textData.getAwtColor();
        font = parseFont(textData.getFont(), textData.getSize(), textData.getStyle());
        align = textData.getAlign();
        wrap = textData.getWrap();
        position = textData.getPosition();
        if (position == null || position.size() != 2) position = null;
        transformOrigin = textData.getOrigin();
        strokeSize = textData.getStrokeSize();
        strokeColor = textData.getStrokeAwtColor();
        build();
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

    private static Font parseFont(String fontName, int size, TextStyle style) {
        return new Font(fontName, style.getValue(), size);
    }

    public void build() {
        int fx = x = pos[0];
        int fy = y = pos[1];
        int maxWidth = width = pos.length >= 3 ? pos[2] : 200;
        font = getFont();
        width = getWidth(font);
        height = getHeight(font);

        switch (align) {
            case CENTER:
                x = fx - width / 2;
                y = fy + height / 2;
                break;
            case RIGHT:
                x = fx - width;
        }

        if (wrap == TextWrap.BREAK && width >= maxWidth) {
            short lineAp = (short) (width / maxWidth);
            StringBuilder builder = new StringBuilder(text);
            short lineWidth = (short) (text.length() / lineAp);
            for (short i = lineAp; i > 0; i--) {
                builder.insert(lineWidth * i, '\n');
            }
            text = builder.toString();
            width = getWidth(font);
            height = getHeight(font);
        }
    }

    /**
     * 获取构建后的文本数据
     */
    public String getText() {
        return text;
    }

    /**
     * 获取构建后的坐标(返回新对象)
     *
     * @return int[4]{x, y, width, height}
     */
    public int[] getPos() {
        return new int[]{x, y, width, height};
    }

    /**
     * 获取颜色 (默认为 #191919)
     */
    public Color getColor() {
        return color;
    }

    public Font zoomFont(float multiple) {
        return new Font(font.getFontName(), font.getStyle(), Math.round(font.getSize() * multiple));
    }

    public List<Position> getPosition() {
        return position;
    }

    /**
     * 获取构建后的字体
     */
    public Font getFont() {
        if (!zoomed && wrap == TextWrap.ZOOM) {
            float multiple = Math.min(1.0F, (float) pos[2] / this.getWidth(font));
            font = zoomFont(multiple);
            width = this.pos[2];
            height = getHeight(font);
            zoomed = true;
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
        AffineTransform old = null;
        if (angle != 0) {
            old = g2d.getTransform();
            if (transformOrigin == TransformOrigin.CENTER) {
                g2d.rotate(Math.toRadians(angle), (float) width / 2 + x, (float) height / 2 + y);
            } else {
                g2d.rotate(Math.toRadians(angle), x, y);
            }
        }

        if (position == null) {
            ImageSynthesisCore.g2dDrawText(
                    g2d, getText(), getPos(),
                    color, getFont()
            );
            return;
        }
        int fx = x, fy = y;
        switch (position.get(0)) {
            case RIGHT:
                fx = stickerWidth - fx;
                break;
            case CENTER:
                fx = stickerWidth / 2 + fx;
                break;
        }
        switch (position.get(1)) {
            case BOTTOM:
                fy = stickerHeight - fy;
                break;
            case CENTER:
                fy = stickerHeight / 2 + fy;
                break;
        }

        int[] fPos = new int[]{fx, fy, width, height};
        if (strokeSize != 0) {
            ImageSynthesisCore.g2dDrawStrokeText(
                    g2d, getText(), fPos,
                    color, getFont(), strokeSize, strokeColor
            );
            return;
        }

        ImageSynthesisCore.g2dDrawText(g2d, getText(), fPos, color, getFont());

        if (angle != 0) g2d.setTransform(old);
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
        return fm.getHeight(); // 使用 getHeight() 来计算字体高度
    }
}
