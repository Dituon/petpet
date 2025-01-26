package moe.dituon.petpet.core.clip;

import moe.dituon.petpet.core.length.Length;
import moe.dituon.petpet.core.length.LengthContext;
import moe.dituon.petpet.core.length.LengthType;
import moe.dituon.petpet.core.length.PercentageLength;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;


public class BorderRadius extends ClipPath {
    public static final BorderRadius ROUND = BorderRadius.fromString("50%");

    protected final PercentageLength leftTop;
    protected final PercentageLength rightTop;
    protected final PercentageLength leftBottom;
    protected final PercentageLength rightBottom;
    protected final boolean isEllipse;

    public BorderRadius(PercentageLength leftTop, PercentageLength rightTop, PercentageLength leftBottom, PercentageLength rightBottom) {
        this.leftTop = leftTop;
        this.rightTop = rightTop;
        this.leftBottom = leftBottom;
        this.rightBottom = rightBottom;
        this.isEllipse = leftTop.getType() == LengthType.PERCENT && leftTop.getRawValue() == 50f
                && leftTop == rightTop && rightBottom == leftBottom && rightTop == leftBottom;
    }

    public BorderRadius(PercentageLength radius) {
        this(radius, radius, radius, radius);
    }

    public BorderRadius(PercentageLength ltAndRb, PercentageLength rtAndLb) {
        this(ltAndRb, rtAndLb, rtAndLb, ltAndRb);
    }

    public BorderRadius(PercentageLength lt, PercentageLength rtAndLb, PercentageLength rb) {
        this(lt, rtAndLb, rtAndLb, rb);
    }

    /**
     * Creates a shape object based on the given position.
     */
    public Shape getShape(RealPosition position) {
        if (this.isEllipse) {
            return new Ellipse2D.Float(position.x, position.y, position.width, position.height);
        }

        var context = position.toLengthContext();
        float x = position.x;
        float y = position.y;
        float w = position.width;
        float h = position.height;
        float leftTopX = this.leftTop.getValue(context, w) + x;
        float leftTopY = this.leftTop.getValue(context, h) + y;
        float rightTopX = this.rightTop.getValue(context, w) + x;
        float rightTopY = this.rightTop.getValue(context, h) + y;
        float leftBottomX = this.leftBottom.getValue(context, w) + x;
        float leftBottomY = this.leftBottom.getValue(context, h) + y;
        float rightBottomX = this.rightBottom.getValue(context, w) + x;
        float rightBottomY = this.rightBottom.getValue(context, h) + y;


        Path2D path = new Path2D.Float();
        path.moveTo(leftTopX + x, y);
        path.lineTo(w - rightTopX + x, y);
        path.curveTo(w - rightTopX / 2 + x, y, w + x, rightTopY / 2 + y, w + x, rightTopY + y);
        path.lineTo(w + x, h - rightBottomY + y);
        path.curveTo(w + x, h - rightBottomY / 2 + y, w - rightBottomX / 2 + x, h + y, w - rightBottomX + x, h + y);
        path.lineTo(leftBottomX + x, h + y);
        path.curveTo(leftBottomX / 2 + x, h + y, x, h - leftBottomY / 2 + y, x, h - leftBottomY + y);
        path.lineTo(x, leftTopY + y);
        path.curveTo(x, leftTopY / 2 + y, leftTopX / 2 + x, y, leftTopX + x, y);
        path.closePath();
        return path;
    }

    /**
     * Builds a new image with the given image and context.
     *
     * @see #getShape(RealPosition)
     */
    public BufferedImage buildImage(BufferedImage image, LengthContext context) {
        var base = new BufferedImage(image.getWidth(), image.getHeight(),
                image.getColorModel().getTransparency() == Transparency.OPAQUE
                        ? BufferedImage.TYPE_4BYTE_ABGR : image.getType()
        );
        Graphics2D g2d = base.createGraphics();
        g2d.setClip(getShape(RealPosition.fromLengthContext(context)));
        g2d.drawImage(image, 0, 0, null);
        return base;
    }

    protected void arcTo(Path2D path, float x, float y, float wSize, float bezierRadius, float bezierAngle) {
        float leftTopFirstPointX = x + wSize * (float) Math.cos(bezierAngle);
        float leftTopFirstPointY = y + wSize * (float) Math.sin(bezierAngle);

        path.lineTo(leftTopFirstPointX, leftTopFirstPointY);

        float leftTopFirstBezierX = x + bezierRadius * (float) Math.cos(bezierAngle + Math.PI / 2);
        float firstBezierY = y + bezierRadius * (float) Math.sin(bezierAngle + Math.PI / 2);
        path.quadTo(leftTopFirstBezierX, firstBezierY, x, y);
    }

    public static BorderRadius fromString(String str) {
        var tokenList = Length.splitString(str);
        switch (tokenList.size()) {
            case 1: {
                var len = PercentageLength.fromString(tokenList.get(0));
                return new BorderRadius(len, len, len, len);
            }
            case 2: {
                var lt = PercentageLength.fromString(tokenList.get(0));
                var rt = PercentageLength.fromString(tokenList.get(1));
                return new BorderRadius(lt, rt, rt, lt);
            }
            case 3: {
                var lt = PercentageLength.fromString(tokenList.get(0));
                var rt = PercentageLength.fromString(tokenList.get(1));
                var rb = PercentageLength.fromString(tokenList.get(2));
                return new BorderRadius(lt, rt, rt, rb);
            }
            case 4: {
                var lt = PercentageLength.fromString(tokenList.get(0));
                var rt = PercentageLength.fromString(tokenList.get(1));
                var rb = PercentageLength.fromString(tokenList.get(2));
                var lb = PercentageLength.fromString(tokenList.get(3));
                return new BorderRadius(lt, rt, lb, rb);
            }
            default:
                throw new IllegalArgumentException("borderRadius must has 1 ~ 4 argument");
        }
    }

    @Override
    public String toString() {
        return this.leftTop.toString() + " " + this.rightTop.toString() + " " + this.leftBottom.toString() + " " + this.rightBottom.toString();
    }
}
