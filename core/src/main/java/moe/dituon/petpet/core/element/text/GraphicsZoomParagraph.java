package moe.dituon.petpet.core.element.text;

import lombok.Getter;
import moe.dituon.petpet.core.length.LengthContext;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * 实现自动缩放简单排版, 需要指定 width
 *
 * @author Dituon
 * @see GraphicsParagraph
 * @see GraphicsBreakParagraph
 */
public class GraphicsZoomParagraph extends GraphicsParagraph {
    protected Font font;
    @Getter
    protected final boolean isAbsolute;
    @Getter
    protected boolean isInitialized = false;

    public GraphicsZoomParagraph(GraphicsAttributedString string) {
        super(string);
        this.font = string.font;
        this.isAbsolute = string.width.isAbsolute();
        if (isAbsolute) {
            zoomText((int) string.width.getValue());
        }
    }

    public GraphicsZoomParagraph(GraphicsAttributedString string, int width) {
        super(string, false);
        this.font = string.font;
        zoomText(width);
        this.isAbsolute = true;
    }

    public void zoomText(int maxWidth) {
        // 确保最小字体只能放大不能缩小
        float multiple = Math.max(1.0F, (float) maxWidth / this.width);
        int size = Math.min(string.maxFontSize, Math.round(font.getSize() * multiple));
        if (size == font.getSize()) return;
        this.width = 0;
        this.font = new Font(font.getFontName(), font.getStyle(), size);
        build();
    }

    @Override
    protected void setGraphicsHint(Graphics2D g2d) {
        g2d.setFont(font);
        if (string.strokeSize == 0) {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        } else {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
    }

    @Override
    protected void preRender(Graphics2D g2d, LengthContext lengthContext) {
        if (isAbsolute) return;
        this.zoomText((int) this.string.width.getValue(lengthContext));
    }

    @Override
    protected void build() {
        var metrics = container.getFontMetrics(font != null ? font : string.font);
        var lines = this.string.text.split("\n");
        xOffset = new float[lines.length];
        yOffset = new float[lines.length];
        text = new String[lines.length];

        int lineHeight = metrics.getHeight();
        this.height = lineHeight * lines.length;
        int ascent = metrics.getAscent();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty()) {
                xOffset[i] = 0;
                yOffset[i] = (float) lineHeight * i + ascent;
                continue;
            }
            int width = metrics.stringWidth(line);
            this.width = Math.max(this.width, width);
            float x = 0;

            switch (string.align) {
                case CENTER:
                    x -= (float) width / 2;
                    break;
                case RIGHT:
                    x -= width;
                    break;
            }

            text[i] = line;
            xOffset[i] = x;
            yOffset[i] = (float) lineHeight * i + ascent;
        }

        switch (string.baseline) {
            case MIDDLE:
                baselineOffset = (float) this.height / 2;
                break;
            case BOTTOM:
                baselineOffset = this.height;
                break;
        }

        if (this.string.strokeSize > 0) {
            this.stroke = new BasicStroke(string.strokeSize, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
        }
        this.isInitialized = true;
    }

    @Override
    protected void buildShapes(float x, float y, Shape[] shapes) {
        for (int i = 0; i < text.length; i++) {
            AffineTransform transform = new AffineTransform();
            transform.translate(x + xOffset[i], y + yOffset[i] - baselineOffset);
            var vector = font.createGlyphVector(getFontRenderContext(), text[i]);
            shapes[i] = transform.createTransformedShape(vector.getOutline());
        }
    }
}
