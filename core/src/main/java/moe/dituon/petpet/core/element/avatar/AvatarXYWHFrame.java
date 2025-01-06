package moe.dituon.petpet.core.element.avatar;

import lombok.Getter;
import moe.dituon.petpet.core.clip.ClipPath;
import moe.dituon.petpet.core.context.CanvasContext;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.element.ElementFrame;
import moe.dituon.petpet.core.imgres.ImageFrameList;
import moe.dituon.petpet.core.length.Length;
import moe.dituon.petpet.core.length.LengthContext;
import moe.dituon.petpet.core.length.LengthType;
import moe.dituon.petpet.core.position.AvatarXYWHCoords;
import moe.dituon.petpet.template.element.AvatarTemplate;
import net.coobird.thumbnailator.Thumbnails;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class AvatarXYWHFrame extends AvatarFrame {
    public final AvatarXYWHCoords coords;
    public final double theta;
    protected int expectedWidth = -1;
    protected int expectedHeight = -1;

    public AvatarXYWHFrame(int index, AvatarTemplate data) {
        super(index, data);
        this.coords = (AvatarXYWHCoords) super.getNElement(data.getCoords());
        this.theta = Math.toRadians(super.angle % 360);
    }

    @Override
    public int getExpectedWidth() {
        if (this.expectedWidth != -1) {
            return this.expectedWidth;
        }
        Length widthLength = this.coords.coordsList.get(2);
        this.expectedWidth = widthLength.isAbsolute() ? (int) widthLength.getValue() : 0;
        return this.expectedWidth;
    }

    @Override
    public int getExpectedHeight() {
        if (this.expectedHeight != -1) {
            return this.expectedHeight;
        }
        Length heightLength = this.coords.coordsList.get(3);
        this.expectedHeight = heightLength.isAbsolute() ? (int) heightLength.getValue() : 0;
        return this.expectedHeight;
    }

    protected static BufferedImage resamplingImage(BufferedImage image, int w, int h) {
        if (image.getWidth() > w * 2 || image.getHeight() > h * 2) {
            return image;
        }
        int min = Math.min(w, h);
        try {
            return Thumbnails.of(image).size(min, min).keepAspectRatio(true).asBufferedImage();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
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
        protected int[] coords;
        protected LengthContext lengthContext;
        protected BufferedImage img;
        /**
         * Original width
         */
        protected int ow;
        /**
         * Original height
         */
        protected int oh;
        /**
         * Real width
         */
        protected int rw;
        /**
         * Real height
         */
        protected int rh;
        @Getter
        public final int width;
        @Getter
        public final int height;
        @Getter
        public final int length;

        protected final ImageFrameList frames;

        protected RenderedFrame(CanvasContext canvasContext, RequestContext requestContext) {
            this.canvasContext = canvasContext;
            this.requestContext = requestContext;
            if (AvatarXYWHFrame.this.coords.isEmpty) {
                this.width = 0;
                this.height = 0;
                this.length = 0;
                this.frames = null;
                return;
            }
            this.frames = requestContext.getFrameList(
                    AvatarXYWHFrame.this.id,
                    AvatarXYWHFrame.this.defaultUrl,
                    AvatarXYWHFrame.this.template.getBasePath()
            );
            this.length = frames.size();
            var image = getNElement(frames).image;
            this.ow = image.getWidth();
            this.oh = image.getHeight();
            image = AvatarXYWHFrame.super.cropper.crop(image, canvasContext.getWidth(), canvasContext.getHeight());
            image = filterList.filter(image, index, template, requestContext);
            this.rw = image.getWidth();
            this.rh = image.getHeight();
            this.img = image;
            this.lengthContext = canvasContext.createLengthContext(ow, oh);
            this.coords = AvatarXYWHFrame.this.coords.getValue(lengthContext);
            int w = coords[2];
            int h = coords[3];
            if (w == 0) {
                w = (int) (h * (float) rw / rh);
            } else if (h == 0) {
                h = (int) (w * (float) rh / rw);
            }
            this.width = w;
            this.height = h;
        }

        public RenderedFrame(RenderedFrame that, int index) {
            this.canvasContext = that.canvasContext;
            this.requestContext = that.requestContext;
            this.coords = that.coords;
            this.lengthContext = that.lengthContext;
            this.ow = that.ow;
            this.oh = that.oh;
            this.rw = that.rw;
            this.rh = that.rh;
            this.width = that.width;
            this.height = that.height;
            this.length = that.length;

            this.frames = that.frames;
            if (frames == null) return;
            var image = ElementFrame.getNElement(frames, index).image;
            image = AvatarXYWHFrame.super.cropper.crop(image, canvasContext.getWidth(), canvasContext.getHeight());
            // TODO: 缓存结果
            image = filterList.filter(image, index, template, requestContext);
            this.img = image;
        }

        @Override
        public void draw() {
            this.draw(index);
        }

        @Override
        public void draw(int customIndex) {
            if (AvatarXYWHFrame.this.coords.isEmpty) return;

            int x = coords[0];
            int y = coords[1];
            int w = this.width;
            int h = this.height;
            if (w == 0 || h == 0) return;

            var g2d = canvasContext.getGraphics(customIndex);
            AffineTransform prevTransform = null;
            double fTheta = theta;
            if (rotateTransition != null && !rotateTransition.isEmpty()) {
                fTheta = Math.toRadians(rotateTransition.getAngle(canvasContext.getLength(), customIndex));
            }
            if (fTheta != 0) {
                // FEATURE: rotate
                var realSizeLengthContext = canvasContext.createLengthContext(w, h);
                var transform = AffineTransform.getRotateInstance(
                        fTheta,
                        x + origin.xOffset.getValue(realSizeLengthContext, w),
                        y + origin.yOffset.getValue(realSizeLengthContext, h)
                );
                prevTransform = g2d.getTransform();
                g2d.setTransform(transform);
            }
            Shape prevClip = null;
            if (AvatarXYWHFrame.super.borderRadius != null) {
                prevClip = g2d.getClip();
                var shape = AvatarXYWHFrame.super.borderRadius.getShape(
                        new ClipPath.RealPosition(
                                lengthContext.canvasWidth, lengthContext.canvasHeight,
                                x, y, w, h
                        )
                );
                g2d.setClip(shape);
            }

            // FEATURE: alpha
            g2d.setComposite(AvatarXYWHFrame.super.alphaComposite);
            // FEATURE: fit
            switch (AvatarXYWHFrame.super.fit) {
                case COVER: {
                    int sx;
                    int sy;
                    int sw;
                    int sh;
                    if ((float) rw / rh < (float) w / h) {
                        sw = rw;
                        sh = (int) (h * ((float) rw / w));
                        sx = (int) (position.xOffset.getValue(lengthContext, (rw - sw)));
                        sy = (int) (position.yOffset.getValue(lengthContext, (rh - sh)));
                    } else {
                        sh = rh;
                        sw = (int) (w * ((float) rh / h));
                        sx = (int) (position.xOffset.getValue(lengthContext, (rw - sw)));
                        sy = (int) (position.yOffset.getValue(lengthContext, (rh - sh)));
                    }
                    if (position.xOffset.getType() != LengthType.PERCENT) {
                        sx = -sx;
                    }
                    if (position.yOffset.getType() != LengthType.PERCENT) {
                        sy = -sy;
                    }
                    g2d.drawImage(img,
                            x, y, w + x, h + y,
                            sx, sy, sw + sx, sh + sy,
                            null
                    );
                    break;
                }
                case CONTAIN: {
                    float sRatio = (float) rw / rh;
                    int dx;
                    int dy;
                    int dw;
                    int dh;
                    if (sRatio < (float) w / h) {
                        dh = h;
                        dw = (int) (dh * sRatio);
                        dx = (int) position.xOffset.getValue(lengthContext, w - dw);
                        dy = (int) position.yOffset.getValue(lengthContext, 0);
                    } else {
                        dw = w;
                        dh = (int) (dw / sRatio);
                        dx = (int) position.xOffset.getValue(lengthContext, 0);
                        dy = (int) position.yOffset.getValue(lengthContext, h - dh);
                    }
                    g2d.drawImage(img,
                            dx + x, dy + y,
                            dw, dh,
                            null
                    );
                    break;
                }
                case FILL:
                default:
                    g2d.drawImage(img, x, y, w, h, null);
            }
            if (prevTransform != null) g2d.setTransform(prevTransform);
            g2d.setClip(prevClip);
        }

        @Override
        public RenderedFrame cloneByIndex(int index) {
            return new RenderedFrame(this, index);
        }
    }
}
