package moe.dituon.petpet.share.element.text;

import moe.dituon.petpet.share.TextAlign;
import moe.dituon.petpet.share.TextBaseline;
import moe.dituon.petpet.share.TransformOrigin;

import java.awt.*;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

/**
 * 实现自动换行简单排版, 需要指定 width
 *
 * @author Dituon
 * @see GraphicsParagraph
 * @see GraphicsZoomParagraph
 */
public class GraphicsBreakParagraph extends GraphicsParagraph {
    protected TextLayout[] text;

    public GraphicsBreakParagraph(GraphicsAttributedString string, TextAlign align, TextBaseline baseline) {
        super(string, align, baseline, false);
    }

    public GraphicsBreakParagraph(GraphicsAttributedString string, TextAlign align, TextBaseline baseline, int width) {
        super(string, align, baseline, false);
        breakText(width);
    }

    public void breakText(int maxWidth) {
        var iter = this.string.getIterator();
        int paragraphStart = iter.getBeginIndex();
        int paragraphEnd = iter.getEndIndex();
        var lineMeasurer = new LineBreakMeasurer(iter, getFontRenderContext());

        xOffset = new float[paragraphEnd];
        yOffset = new float[paragraphEnd];
        text = new TextLayout[paragraphEnd];

        float drawPosY = 0;

        lineMeasurer.setPosition(paragraphStart);

        for (int i = 0; lineMeasurer.getPosition() < paragraphEnd; i++) {
            int next = lineMeasurer.nextOffset(maxWidth);
            int limit = next;
            var charIndex = this.string.text.indexOf('\n', lineMeasurer.getPosition() + 1);
            if (next > (charIndex - lineMeasurer.getPosition()) && charIndex != -1) {
                limit = charIndex - lineMeasurer.getPosition();
            }
            var layout = lineMeasurer.nextLayout(maxWidth, lineMeasurer.getPosition() + limit, false);

            float drawPosX = layout.isLeftToRight()
                    ? 0 : maxWidth - layout.getAdvance();

            var advance = layout.getAdvance();
            super.width = Math.max((int) advance, super.width);
            switch (align) {
                case CENTER:
                    drawPosX -= advance / 2;
                    break;
                case RIGHT:
                    drawPosX -= advance;
                    break;
            }

            drawPosY += layout.getAscent();
            text[i] = layout;
            xOffset[i] = drawPosX;
            yOffset[i] = drawPosY;
            drawPosY += layout.getDescent() + layout.getLeading();
        }
        super.height = Math.round(drawPosY);

        switch (baseline) {
//            case ALPHABETIC:
//                baselineOffset = -ascent;
//                break;
            case MIDDLE:
                baselineOffset = drawPosY / 2;
                break;
            case BOTTOM:
                baselineOffset = drawPosY;
                break;
        }
    }

    @Override
    protected void setGraphicsHint(Graphics2D g2d) {
        g2d.setFont(string.font);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    @Override
    public void draw(Graphics2D g2d, float x, float y) {
        if (text == null) {
            throw new RuntimeException("break width not set");
        }
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

        if (this.string.strokeSize == 0) {
            g2d.setColor(string.color);
            for (int i = 0; i < text.length; i++) {
                if (text[i] == null) return;
                text[i].draw(g2d, x + xOffset[i], y + yOffset[i] - baselineOffset);
            }
            return;
        }

        if (stroke == null) {
            stroke = new BasicStroke(this.string.strokeSize, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
        }
        if (shape == null) {
            shape = new Shape[text.length];
            for (int i = 0; i < text.length; i++) {
                AffineTransform transform = new AffineTransform();
                transform.translate(x + xOffset[i], y + yOffset[i] - baselineOffset);
                shape[i] = text[i].getOutline(transform);
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
