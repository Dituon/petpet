package moe.dituon.petpet.core.element.avatar;

import lombok.Getter;
import moe.dituon.petpet.core.context.CanvasContext;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.element.ElementFrame;
import moe.dituon.petpet.core.imgres.ImageFrameList;
import moe.dituon.petpet.core.length.LengthContext;
import moe.dituon.petpet.core.position.AvatarP4ACoords;
import moe.dituon.petpet.core.utils.image.ImageDeformer;
import moe.dituon.petpet.template.element.AvatarTemplate;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class AvatarP4AFrame extends AvatarFrame {
    public final AvatarP4ACoords coords;
    public final double theta;
    protected int expectedWidth = -1;
    protected int expectedHeight = -1;

    public AvatarP4AFrame(int index, AvatarTemplate data) {
        super(index, data);

        this.coords = (AvatarP4ACoords) super.getNElement(data.getCoords());
        this.theta = Math.toRadians(super.angle % 360);
    }

    @Override
    public int getExpectedWidth() {
        if (this.expectedWidth != -1) {
            return this.expectedWidth;
        }
        initExpectedSize();
        return this.expectedWidth;
    }

    @Override
    public int getExpectedHeight() {
        if (this.expectedHeight != -1) {
            return this.expectedHeight;
        }
        initExpectedSize();
        return this.expectedHeight;
    }

    protected void initExpectedSize() {
        if (!this.coords.isAbsolute) {
            this.expectedWidth = 0;
            this.expectedHeight = 0;
        }
        int[] xPoints = new int[4];
        int[] yPoints = new int[4];
        Point2D[] ps = this.coords.getValue();
        for (int i = 0; i < 4; i++) {
            xPoints[i] = (int) ps[i].getX();
            yPoints[i] = (int) ps[i].getY();
        }
        var bounds = new Polygon(xPoints, yPoints, 4).getBounds();
        this.expectedWidth = bounds.width;
        this.expectedHeight = bounds.height;
    }

    @Override
    public void draw(CanvasContext canvasContext, RequestContext requestContext) {
        this.draw(canvasContext, requestContext, this.index);
    }

    @Override
    public void draw(CanvasContext canvasContext, RequestContext requestContext, int index) {
        var renderedFrame = new RenderedFrame(canvasContext, requestContext);
        renderedFrame.draw(index);
    }

    @Override
    public ElementFrame.RenderedFrame render(CanvasContext canvasContext, RequestContext requestContext) {
        return new RenderedFrame(canvasContext, requestContext);
    }

    public class RenderedFrame extends ElementFrame.RenderedFrame {
        protected final CanvasContext canvasContext;
        protected final RequestContext requestContext;
        protected Point2D[] coords;
        protected LengthContext lengthContext;
        protected BufferedImage img;
        @Getter
        public final int width;
        @Getter
        public final int height;
        @Getter
        public final int length;

        protected final ImageFrameList frames;

        public RenderedFrame(CanvasContext canvasContext, RequestContext requestContext) {
            this.canvasContext = canvasContext;
            this.requestContext = requestContext;

            //TODO: fit
            if (AvatarP4AFrame.this.coords.isEmpty) {
                this.width = 0;
                this.height = 0;
                this.length = 0;
                this.frames = null;
                return;
            }
            this.frames = requestContext.getFrameList(
                    AvatarP4AFrame.this.id, AvatarP4AFrame.this.defaultUrl, AvatarP4AFrame.this.template.getBasePath()
            );
            int start = template.getStart(), end = template.getEnd();
            if (end < 0) {
                end = frames.size() + end + 1;
            }
            this.length = end - start;
            var image = getNElement(frames).image;
            image = AvatarP4AFrame.super.cropper.crop(image, canvasContext.getWidth(), canvasContext.getHeight());
            image = filterList.filter(image, index, template, requestContext);
            this.lengthContext = canvasContext.createLengthContext(image.getWidth(), image.getHeight());
            this.coords = AvatarP4AFrame.this.coords.getValue(lengthContext);
            ImageDeformer.toAbsoluteCoords(coords);
            if (borderRadius != null) {
                image = borderRadius.buildImage(image, lengthContext);
            }
            this.img = ImageDeformer.computeImage(image, coords);
            this.width = this.img.getWidth();
            this.height = this.img.getHeight();
        }

        public RenderedFrame(RenderedFrame that, int index) {
            this.canvasContext = that.canvasContext;
            this.requestContext = that.requestContext;
            this.coords = that.coords;
            this.lengthContext = that.lengthContext;
            this.width = that.width;
            this.height = that.height;
            this.length = that.length;
            this.frames = that.frames;
            if (frames == null) return;

            var image = ElementFrame.getNElement(frames, index).image;
            image = AvatarP4AFrame.super.cropper.crop(image, canvasContext.getWidth(), canvasContext.getHeight());
            image = filterList.filter(image, index, template, requestContext);
            if (borderRadius != null) {
                image = borderRadius.buildImage(image, lengthContext);
            }
            this.img = ImageDeformer.computeImage(image, coords);

        }

        @Override
        public void draw() {
            this.draw(index);
        }

        @Override
        public void draw(int customIndex) {
            if (AvatarP4AFrame.this.coords.isEmpty) return;

            var anchor = coords[4];
            int anchorX = (int) anchor.getX();
            int anchorY = (int) anchor.getY();

            var g2d = canvasContext.getGraphics(customIndex);
            AffineTransform prevTransform = null;
            double fTheta = theta;
            if (rotateTransition != null && !rotateTransition.isEmpty()) {
                fTheta = Math.toRadians(rotateTransition.getAngle(canvasContext.getLength(), customIndex));
            }
            if (AvatarP4AFrame.this.theta != 0) {
                // FEATURE: rotate
                var transform = AffineTransform.getRotateInstance(
                        fTheta,
                        anchorX + origin.xOffset.getValue(lengthContext),
                        anchorY + origin.yOffset.getValue(lengthContext)
                );
                prevTransform = g2d.getTransform();
                g2d.setTransform(transform);
            }
            // FEATURE: alpha
            g2d.setComposite(AvatarP4AFrame.super.alphaComposite);

            g2d.drawImage(this.img, anchorX, anchorY, null);

            if (prevTransform != null) g2d.setTransform(prevTransform);
        }

        @Override
        public RenderedFrame cloneByIndex(int index) {
            return new RenderedFrame(this, index);
        }
    }
}
