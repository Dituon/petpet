package moe.dituon.petpet.core.canvas;

import lombok.Getter;
import moe.dituon.petpet.core.GlobalContext;
import moe.dituon.petpet.core.context.CanvasContext;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.element.Dependable;
import moe.dituon.petpet.core.element.ElementFrame;
import moe.dituon.petpet.core.element.background.BackgroundModel;
import moe.dituon.petpet.core.length.Length;
import moe.dituon.petpet.core.length.LengthContext;
import moe.dituon.petpet.core.length.NumberLength;
import moe.dituon.petpet.template.TemplateCanvas;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.*;

public class CanvasModel implements Dependable {
    public final TemplateCanvas template;
    protected final Length width;
    protected final Length height;
    @Getter
    public final Length length;
    protected int staticLength = 0;
    @Getter
    public final boolean isAbsolute;
    protected int absoluteWidth = 0;
    protected int absoluteHeight = 0;
    @Nullable
    @Getter
    protected final BackgroundModel backgroundModel;
    protected Set<String> dependentIds = null;
    protected final boolean backgroundAtBottom;

    public CanvasModel(TemplateCanvas template) {
        this(template, null, false);
    }

    public CanvasModel(TemplateCanvas template, BackgroundModel backgroundModel) {
        this(template, backgroundModel, true);
    }

    public CanvasModel(
            TemplateCanvas template,
            @Nullable BackgroundModel backgroundModel,
            boolean backgroundAtBottom
    ) {
        this.template = template;
        this.backgroundModel = backgroundModel;
        this.backgroundAtBottom = backgroundAtBottom;
        this.width = template.getWidth();
        this.height = template.getHeight();
        if (!template.isSizeDefined() && backgroundModel == null) {
            throw new IllegalArgumentException("Must specify canvas size or define a background");
        }
        this.length = backgroundModel == null ? template.getLength() : NumberLength.px(backgroundModel.length);
        if (length.isAbsolute()) {
            this.staticLength = (int) length.getValue();
            if (staticLength <= 0 || staticLength >= 65536) {
                throw new IllegalArgumentException("Invalid canvas length: " + staticLength);
            }
        }
        boolean sizeIsAbsolute = template.isSizeDefined() && (width.isAbsolute() && height.isAbsolute());
        this.isAbsolute = backgroundModel != null || sizeIsAbsolute;
        if (sizeIsAbsolute) {
            this.absoluteWidth = Math.round(width.getValue());
            this.absoluteHeight = Math.round(height.getValue());
        }
    }

    public CanvasContext createCanvasContext(Map<String, Integer> variables) {
        int lengthValue;
        if (this.length.isAbsolute()) {
            lengthValue = (int) this.length.getValue();
        } else {
            var lengthContext = new LengthContext(0, 0, 0, 0);
            lengthContext.variables = variables;
            lengthValue = (int) this.length.getValue(lengthContext);
        }

        return new CanvasContext(
                lengthValue,
                variables,
                // TODO: context render config
                GlobalContext.getInstance().getRenderConfig(),
                template.getReverse()
        );
    }

    public List<BufferedImage> createCanvasImages(CanvasContext canvasContext, RequestContext requestContext) {
        if (this.backgroundModel != null) {
            if (this.backgroundAtBottom) {
                return this.backgroundModel.getImages();
            } else {
                return createImageList(backgroundModel.getLength(), backgroundModel.getWidth(), backgroundModel.getHeight());
            }
        }


        if (this.isAbsolute) {
            if (staticLength == 0) {
                return this.createImageList(
                        (int) length.getValue(canvasContext.getLengthContext()),
                        this.absoluteWidth, this.absoluteHeight
                );
            } else {
                return this.createImageList(staticLength, this.absoluteWidth, this.absoluteHeight);
            }
        }

        var lengthContext = canvasContext.getLengthContext();
        int w = (int) this.width.getValue(lengthContext);
        int h = (int) this.height.getValue(lengthContext);
        if (staticLength == 0) {
            return this.createImageList((int) this.length.getValue(lengthContext), w, h);
        } else {
            return this.createImageList(staticLength, w, h);
        }
    }

    protected List<BufferedImage> createImageList(int length, int width, int height) {
        var list = new ArrayList<BufferedImage>(length);
        for (int i = 0; i < length; i++) {
            list.add(this.createImage(length, i, width, height));
        }
        return list;
    }

    protected BufferedImage createImage(int length, int i, int width, int height) {
        var img = new BufferedImage(width, height, length == 1 ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_3BYTE_BGR);
        var g2d = img.createGraphics();
        var color = ElementFrame.getNElement(template.getColor(), i);
        if (color.getAlpha() != 0) {
            g2d.setColor(color);
            g2d.fillRect(0, 0, width, height);
        }
        return img;
    }

    @Override
    public Set<String> getDependentIds() {
        if (this.isAbsolute) return Collections.emptySet();
        if (this.dependentIds != null) return this.dependentIds;
        int size = (this.width == null ? 0 : this.width.getDependentIds().size())
                + (this.height == null ? 0 : this.height.getDependentIds().size());
        this.dependentIds = new HashSet<>(size);
        if (this.width != null) this.dependentIds.addAll(this.width.getDependentIds());
        if (this.height != null) this.dependentIds.addAll(this.height.getDependentIds());
        return this.dependentIds;
    }
}
