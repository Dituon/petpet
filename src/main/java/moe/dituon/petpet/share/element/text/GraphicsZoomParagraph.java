package moe.dituon.petpet.share.element.text;

import moe.dituon.petpet.share.TextAlign;
import moe.dituon.petpet.share.TextBaseline;

import java.awt.*;

/**
 * 实现自动缩放简单排版, 需要指定 width
 *
 * @author Dituon
 * @see GraphicsParagraph
 * @see GraphicsBreakParagraph
 */
public class GraphicsZoomParagraph extends GraphicsParagraph {
    protected Font font;

    public GraphicsZoomParagraph(GraphicsAttributedString string, TextAlign align, TextBaseline baseline) {
        super(string, align, baseline);
        this.font = string.font;
    }

    public GraphicsZoomParagraph(GraphicsAttributedString string, TextAlign align, TextBaseline baseline, int width) {
        super(string, align, baseline);
        font = string.font;
        zoomText(width);
    }

    public void zoomText(int maxWidth) {
        float multiple = Math.min(1.0F, (float) maxWidth / this.width);
        if (multiple == 1.0F) return;
        this.width = 0;
        this.font = new Font(font.getFontName(), font.getStyle(), (int) (font.getSize() * multiple));
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
}
