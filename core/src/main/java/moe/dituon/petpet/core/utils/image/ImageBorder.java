package moe.dituon.petpet.core.utils.image;

import moe.dituon.petpet.core.length.Length;
import moe.dituon.petpet.core.length.LengthContext;
import moe.dituon.petpet.core.length.NumberLength;
import moe.dituon.petpet.template.fields.ColorKt;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ImageBorder {
    public final Length length;
    public final Color color;
    protected final boolean isAbsolute;
    protected final BasicStroke staticStroke;

    public ImageBorder(@NotNull Length length, Color color) {
        this.length = length;
        this.color = color;
        this.isAbsolute = length.isAbsolute();
        this.staticStroke = isAbsolute ? new BasicStroke(length.getValue()) : null;
    }

    public void draw(Graphics2D g2d, Shape borderShape) {
        draw(g2d, borderShape, null);
    }

    public void draw(Graphics2D g2d, Shape borderShape, LengthContext context) {
        g2d.setColor(color);
        if (isAbsolute) {
            g2d.setStroke(staticStroke);
        } else if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        } else {
            g2d.setStroke(new BasicStroke(length.getValue(context)));
        }
        if (staticStroke.getLineWidth() <= 0f) return;
        g2d.draw(borderShape);
    }

    public void draw(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(color);
        g2d.setStroke(staticStroke);
        g2d.drawRect(x, y, width, height);
    }

    public static class EmptyImageBorder extends ImageBorder {
        public static final EmptyImageBorder INSTANCE = new EmptyImageBorder();

        protected EmptyImageBorder() {
            super(NumberLength.EMPTY, null);
        }

        @Override
        public void draw(Graphics2D g2d, Shape borderShape) {
        }

        @Override
        public void draw(Graphics2D g2d, Shape borderShape, LengthContext context) {
        }
    }

    public static ImageBorder empty() {
        return EmptyImageBorder.INSTANCE;
    }

    public static ImageBorder fromString(String str) {
        var parts = Length.splitString(str);
        Color color = Color.BLACK;
        Length length = null;
        switch (parts.size()) {
            case 1:
                if (parts.get(0).startsWith("#")) {
                    color = ColorKt.decodeColor(parts.get(0));
                } else {
                    length = Length.fromString(parts.get(0));
                }
                break;
            case 2:
                if (parts.get(0).startsWith("#")) {
                    color = ColorKt.decodeColor(parts.get(0));
                    length = Length.fromString(parts.get(1));
                } else {
                    length = Length.fromString(parts.get(0));
                    color = ColorKt.decodeColor(parts.get(1));
                }
                break;
            default:
                throw new IllegalArgumentException("border must has 1 or 2 argument");
        }
        if (length == null || (length.isAbsolute() && length.getValue() <= 0f)) {
            return EmptyImageBorder.INSTANCE;
        }
        return new ImageBorder(length, color);
    }
}
