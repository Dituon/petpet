package moe.dituon.petpet.core.element.text;

import lombok.Getter;
import moe.dituon.petpet.core.length.LengthContext;

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
    protected TextLayout[] textLayouts;
    @Getter
    protected final boolean isAbsolute;

    public GraphicsBreakParagraph(GraphicsAttributedString string) {
        super(string, false);
        this.isAbsolute = string.width.isAbsolute();
        if (isAbsolute) {
            breakText((int) string.width.getValue());
        }
    }

    public GraphicsBreakParagraph(GraphicsAttributedString string, int width) {
        super(string, false);
        this.isAbsolute = true;
        breakText(width);
    }

    public void setWidth(int maxWidth) {
        breakText(maxWidth);
    }

    public boolean isInitialized() {
        return textLayouts != null;
    }

    public void breakText(int maxWidth) {
        var iter = this.string.getIterator();
        int paragraphStart = iter.getBeginIndex();
        int paragraphEnd = iter.getEndIndex();
        var lineMeasurer = new LineBreakMeasurer(iter, getFontRenderContext());

        xOffset = new float[paragraphEnd];
        yOffset = new float[paragraphEnd];
        textLayouts = new TextLayout[paragraphEnd];

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
            switch (string.align) {
                case CENTER:
                    drawPosX -= advance / 2;
                    break;
                case RIGHT:
                    drawPosX -= advance;
                    break;
            }

            drawPosY += layout.getAscent();
            textLayouts[i] = layout;
            xOffset[i] = drawPosX;
            yOffset[i] = drawPosY;
            drawPosY += layout.getDescent() + layout.getLeading();
        }
        super.height = Math.round(drawPosY);

        switch (string.baseline) {
            //TODO
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

        if (this.string.strokeSize > 0) {
            this.stroke = stroke != null ? stroke : new BasicStroke(this.string.strokeSize, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
            initStaticShapes();
        }
    }

    @Override
    protected void buildShapes(float x, float y, Shape[] shapes) {
        for (int i = 0; i < textLayouts.length; i++) {
            AffineTransform transform = new AffineTransform();
            transform.translate(x + xOffset[i], y + yOffset[i] - baselineOffset);
            shapes[i] = textLayouts[i].getOutline(transform);
        }
    }

    @Override
    protected void preRender(Graphics2D g2d, LengthContext lengthContext) {
        if (isAbsolute) return;
        this.breakText((int) this.string.width.getValue(lengthContext));
    }

    @Override
    public void draw(Graphics2D g2d, LengthContext lengthContext) {
        if (textLayouts == null) {
            throw new IllegalArgumentException("break width not set");
        }
        preRender(g2d, lengthContext);
        float x = this.string.x.getValue(lengthContext);
        float y = this.string.y.getValue(lengthContext);
        super.setGraphicsHint(g2d);

        AffineTransform prevTransform = null;
        if (this.string.theta != 0) {
            // FEATURE: rotate
            var transform = AffineTransform.getRotateInstance(
                    this.string.theta,
                    x + super.string.origin.xOffset.getValue(lengthContext),
                    y + super.string.origin.yOffset.getValue(lengthContext)
            );
            prevTransform = g2d.getTransform();
            g2d.setTransform(transform);
        }

        if (this.string.strokeSize == 0) {
            g2d.setColor(string.color);
            for (int i = 0; i < textLayouts.length; i++) {
                if (textLayouts[i] == null) return;
                textLayouts[i].draw(g2d, x + xOffset[i], y + yOffset[i] - baselineOffset);
            }
            return;
        }

        if (stroke == null) {
            stroke = new BasicStroke(this.string.strokeSize, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
        }
        var tempShape = shape;
        if (tempShape == null) {
            tempShape = new Shape[textLayouts.length];
            buildShapes(x, y, tempShape);
        }
        finallyStrokedDraw(g2d, tempShape);
        if (string.theta != 0) g2d.setTransform(prevTransform);
    }
}
