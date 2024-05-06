package moe.dituon.petpet.share.element.text;

import moe.dituon.petpet.share.TextAlign;
import moe.dituon.petpet.share.TextBaseline;
import moe.dituon.petpet.share.TransformOrigin;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.WeakHashMap;

/**
 * 实现简单排版的多行段落, 支持对齐, 基线等
 *
 * @author Dituon
 * @see GraphicsBreakParagraph
 * @see GraphicsZoomParagraph
 */
public class GraphicsParagraph {
    protected static final Graphics2D container = new BufferedImage(1, 1, 1).createGraphics();

    protected static final WeakHashMap<Font, FontRenderContext> renderContextCache = new WeakHashMap<>();

    public static FontRenderContext getFontRenderContext(Font font) {
        if (renderContextCache.containsKey(font)) {
            return renderContextCache.get(font);
        }
        FontRenderContext fontRenderContext = container.getFontMetrics(font).getFontRenderContext();
        renderContextCache.put(font, fontRenderContext);
        return fontRenderContext;
    }

    protected final GraphicsAttributedString string;
    protected float[] xOffset;
    protected float[] yOffset;
    protected String[] text;
    protected float baselineOffset;

    TextAlign align;
    TextBaseline baseline;

    protected BasicStroke stroke;
    protected Shape[] shape;
    protected int width = 0;
    protected int height = 0;

    public GraphicsParagraph(GraphicsAttributedString string, TextAlign align, TextBaseline baseline) {
        this(string, align, baseline, true);
    }

    protected GraphicsParagraph(
            GraphicsAttributedString string, TextAlign align, TextBaseline baseline,
            boolean initFlag
    ) {
        this.align = align;
        this.baseline = baseline;
        this.string = string;
        if (initFlag) build();
    }

    void build() {
        var metrics = container.getFontMetrics(this.string.font);
        var lines = this.string.text.split("\n");
        xOffset = new float[lines.length];
        yOffset = new float[lines.length];
        this.text = new String[lines.length];

        int lineHeight = metrics.getHeight();
        this.height = lineHeight * lines.length;
        int ascent = metrics.getAscent();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty()) {
                xOffset[i] = 0;
                yOffset[i] = lineHeight * i + ascent;
                continue;
            }
            int width = metrics.stringWidth(line);
            this.width = Math.max(this.width, width);
            float x = 0;

            switch (align) {
                case CENTER:
                    x -= (float) width / 2;
                    break;
                case RIGHT:
                    x -= width;
                    break;
            }

            text[i] = line;
            xOffset[i] = x;
            yOffset[i] = lineHeight * i + ascent;
        }

        switch (baseline) {
            case MIDDLE:
                baselineOffset = (float) this.height / 2;
                break;
            case BOTTOM:
                baselineOffset = this.height;
                break;
        }
    }

    /**
     * 设置画布的字体与渲染选项
     */
    protected void setGraphicsHint(Graphics2D g2d) {
        g2d.setFont(this.string.font);
        if (string.strokeSize == 0) {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        } else {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
    }

    public FontRenderContext getFontRenderContext() {
        return getFontRenderContext(this.string.font);
    }

    public void draw(Graphics2D g2d, float x, float y) {
        setGraphicsHint(g2d);

        AffineTransform old = null;
        if (string.angle != 0) {
            old = g2d.getTransform();
            if (string.origin == TransformOrigin.CENTER) {
                g2d.rotate(Math.toRadians(string.angle), (float) width / 2 + x, (float) height / 2 + y);
            } else {
                g2d.rotate(Math.toRadians(string.angle), x, y);
            }
        }

        if (string.strokeSize == 0) {
            g2d.setColor(string.color);
            for (int i = 0; i < text.length; i++) {
                g2d.drawString(text[i], x + xOffset[i], y + yOffset[i] - baselineOffset);
            }
            return;
        }

        if (stroke == null) {
            stroke = new BasicStroke(this.string.strokeSize, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
        }
        if (shape == null) {
            for (int i = 0; i < text.length; i++) {
                AffineTransform transform = new AffineTransform();
                transform.translate(x + xOffset[i], y + yOffset[i] - baselineOffset);
                var vector = string.font.createGlyphVector(getFontRenderContext(), text[i]);
                shape[i] = transform.createTransformedShape(vector.getOutline());
            }
        }
        for (int i = 0; i < text.length; i++) {
            g2d.setColor(string.strokeColor);
            g2d.setStroke(stroke);
            g2d.draw(shape[i]);
            g2d.setColor(string.color);
            g2d.fill(shape[i]);
        }
        if (string.angle != 0) g2d.setTransform(old);
    }
}
