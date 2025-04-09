package moe.dituon.petpet.core.element.text;

import lombok.Getter;
import moe.dituon.petpet.core.context.CanvasContext;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.element.ElementFrame;
import moe.dituon.petpet.core.length.LengthContext;
import moe.dituon.petpet.core.length.LengthType;

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
public class GraphicsParagraph extends ElementFrame {
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

    @Getter
    protected final GraphicsAttributedString string;
    protected float[] xOffset;
    protected float[] yOffset;
    protected String[] text;
    protected float baselineOffset;

    protected BasicStroke stroke = null;
    protected Shape[] shape = null;

    @Getter
    protected int width = 0;
    @Getter
    protected int height = 0;

    public GraphicsParagraph(GraphicsAttributedString string) {
        this(string, true);
    }

    /**
     * @param initFlag 是否初始化 (调用 build 函数)
     */
    protected GraphicsParagraph(
            GraphicsAttributedString string,
            boolean initFlag
    ) {
        if (string.width.getType() == LengthType.CW || string.width.getType() == LengthType.CH) {
            throw new IllegalArgumentException("Width cannot be set to 'cw' or 'ch' in Paragraph");
        }
        this.string = string;
        this.index = string.index;
        if (initFlag) build();
    }

    protected void build() {
        var metrics = container.getFontMetrics(this.string.font);
        var lines = this.string.text.split("\n");
        this.xOffset = new float[lines.length];
        this.yOffset = new float[lines.length];
        this.text = new String[lines.length];

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
            int w = metrics.stringWidth(line);
            this.width = Math.max(this.width, w);
            float x = 0;

            switch (string.align) {
                case CENTER:
                    x -= (float) w / 2;
                    break;
                case RIGHT:
                    x -= w;
                    break;
            }

            text[i] = line;
            xOffset[i] = x;
            yOffset[i] = (float) lineHeight * i + ascent;
        }

        switch (string.baseline) {
            case ALPHABETIC:
                baselineOffset = ascent;
                break;
            case MIDDLE:
                baselineOffset = (float) this.height / 2;
                break;
            case BOTTOM:
                baselineOffset = this.height;
                break;
        }

        if (this.string.strokeSize > 0) {
            this.stroke = new BasicStroke(string.strokeSize, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
            initStaticShapes();
        }
    }

    protected void initStaticShapes() {
        if (this.string.x.isAbsolute() && string.y.isAbsolute()) {
            int x = (int) string.x.getValue();
            int y = (int) string.y.getValue();
            this.shape = new Shape[text.length];
            buildShapes(x, y, shape);
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

    protected void preRender(Graphics2D g2d, LengthContext lengthContext) {
    }

    public FontRenderContext getFontRenderContext() {
        return getFontRenderContext(this.string.font);
    }

    @Override
    public void draw(CanvasContext canvasContext, RequestContext requestContext) {
        this.draw(canvasContext, requestContext, this.index);
    }

    @Override
    public void draw(CanvasContext canvasContext, RequestContext requestContext, int index) {
        this.draw(
                canvasContext.getGraphics(index),
                canvasContext.createLengthContext(this.width, this.height)
        );
    }

    @Override
    public ElementFrame.RenderedFrame render(CanvasContext canvasContext, RequestContext requestContext) {
        return new RenderedFrame(canvasContext, requestContext);
    }

    public void draw(Graphics2D g2d, LengthContext lengthContext) {
        preRender(g2d, lengthContext);
        setGraphicsHint(g2d);
        float x = string.x.getValue(lengthContext);
        float y = string.y.getValue(lengthContext);

        AffineTransform prevTransform = null;
        if (string.theta != 0) {
            // FEATURE: rotate
            var transform = AffineTransform.getRotateInstance(
                    string.theta,
                    x + string.origin.xOffset.getValue(lengthContext),
                    y + string.origin.yOffset.getValue(lengthContext)
            );
            prevTransform = g2d.getTransform();
            g2d.setTransform(transform);
        }
        if (string.strokeSize == 0) {
            g2d.setColor(string.color);
            for (int i = 0; i < text.length; i++) {
                g2d.drawString(text[i], x + xOffset[i], y + yOffset[i] - baselineOffset);
            }
            return;
        }

        var tempShape = shape;
        if (tempShape == null) {
            tempShape = new Shape[text.length];
            buildShapes(x, y, tempShape);
        }
        finallyStrokedDraw(g2d, tempShape);
        if (string.angle != 0) g2d.setTransform(prevTransform);
    }

    protected void buildShapes(float x, float y, Shape[] shapes) {
        for (int i = 0; i < text.length; i++) {
            AffineTransform transform = new AffineTransform();
            transform.translate(x + xOffset[i], y + yOffset[i] - baselineOffset);
            var vector = string.font.createGlyphVector(getFontRenderContext(), text[i]);
            shapes[i] = transform.createTransformedShape(vector.getOutline());
        }
    }

    protected void finallyStrokedDraw(Graphics2D g2d, Shape[] shapes) {
        for (int i = 0; i < text.length; i++) {
            g2d.setColor(string.strokeColor);
            g2d.setStroke(stroke);
            g2d.draw(shapes[i]);
            g2d.setColor(string.color);
            g2d.fill(shapes[i]);
        }
    }

    public class RenderedFrame extends ElementFrame.RenderedFrame {
        @Getter
        public final int width;
        @Getter
        public final int height;
        protected final CanvasContext canvasContext;
        protected final RequestContext requestContext;
        protected final LengthContext lengthContext;

        public RenderedFrame(CanvasContext canvasContext, RequestContext requestContext) {
            this.width = GraphicsParagraph.this.width;
            this.height = GraphicsParagraph.this.height;
            this.canvasContext = canvasContext;
            this.requestContext = requestContext;
            this.lengthContext = canvasContext.createLengthContext(this.width, this.height);
        }

        @Override
        public void draw() {
            this.draw(index);
        }

        @Override
        public void draw(int index) {
            GraphicsParagraph.this.draw(
                    this.canvasContext.getGraphics(index),
                    this.lengthContext
            );
        }

        @Override
        public int getLength() {
            return 1;
        }

        @Override
        public RenderedFrame cloneByIndex(int index) {
            return this;
        }
    }
}
