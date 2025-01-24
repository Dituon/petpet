package moe.dituon.petpet.core.context;

import lombok.Getter;
import lombok.Setter;
import moe.dituon.petpet.core.BaseRenderConfig;
import moe.dituon.petpet.core.imgres.ImageFrame;
import moe.dituon.petpet.core.imgres.ImageFrameList;
import moe.dituon.petpet.core.length.DynamicLength;
import moe.dituon.petpet.core.length.LengthContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.List;
import java.util.*;


public class CanvasContext {
    protected static final BaseRenderConfig DEFAULT_RENDER_CONFIG = new BaseRenderConfig();
    @Getter
    protected int width;
    @Getter
    protected int height;
    @Getter
    protected int length;
    @Getter
    protected ImageFrameList frameList;
    protected List<Graphics2D> graphicsList = null;
    @Getter
    protected final Map<String, Integer> variableMap;
    protected LengthContext staticLengthContext = null;
    @Getter
    protected final BaseRenderConfig renderConfig;
    @Getter
    protected final boolean reverse;
    @Getter
    @Setter
    @Nullable
    protected File basePath = null;

    public CanvasContext(int initLength) {
        this.length = initLength;
        this.variableMap = new HashMap<>(16);
        this.renderConfig = DEFAULT_RENDER_CONFIG;
        this.reverse = false;
    }

    public CanvasContext(
            int initLength,
            @NotNull Map<String, Integer> initVariables,
            @Nullable BaseRenderConfig renderConfig,
            boolean isReverse
    ) {
        this.length = initLength;
        this.variableMap = initVariables;
        this.renderConfig = renderConfig == null ? DEFAULT_RENDER_CONFIG : renderConfig;
        this.reverse = isReverse;
    }

    public CanvasContext(
            @NotNull BufferedImage canvas,
            @NotNull Map<String, Integer> initVariables,
            @Nullable BaseRenderConfig renderConfig,
            boolean isReverse
    ) {
        this.setCanvas(canvas);
        this.variableMap = initVariables;
        this.renderConfig = renderConfig == null ? DEFAULT_RENDER_CONFIG : renderConfig;
        this.reverse = isReverse;
    }

    public CanvasContext(
            @NotNull ImageFrameList frameList,
            @NotNull Map<String, Integer> initVariables,
            @Nullable BaseRenderConfig renderConfig,
            boolean isReverse
    ) {
        this.setCanvasList(frameList);
        this.variableMap = initVariables;
        this.renderConfig = renderConfig == null ? DEFAULT_RENDER_CONFIG : renderConfig;
        this.reverse = isReverse;
    }

    public CanvasContext setCanvasList(@NotNull ImageFrameList frameList) {
        if (frameList.isEmpty()) throw new IllegalArgumentException("frame list is empty");
        this.frameList = frameList;
        this.width = frameList.get(0).image.getWidth();
        this.height = frameList.get(0).image.getHeight();
        this.length = frameList.size();
        this.graphicsList = new ArrayList<>(Collections.nCopies(this.length, null));
        return this;
    }

    public CanvasContext setCanvas(@NotNull BufferedImage canvas) {
        this.frameList = new ImageFrameList(canvas);
        this.width = canvas.getWidth();
        this.height = canvas.getHeight();
        this.length = 1;
        this.graphicsList = List.of(createGraphics(canvas));
        return this;
    }

    public void setLength(int length) {
        if (this.length != 1) {
            throw new IllegalStateException("setLength() can only be called on single frame canvas");
        }
        if (length == 1) {
            return;
        }
        this.length = length;
        var first = frameList.get(0);
        var list = new ArrayList<ImageFrame>(length);
        list.add(first);
        for (int i = 1; i < length; i++) {
            //TODO: delay
            list.add(new ImageFrame(cloneImage(first.image), first.delay));
        }
        this.frameList = new ImageFrameList(list);
        this.graphicsList = new ArrayList<>(Collections.nCopies(length, null));
    }

    public void putSize(String id, int width, int height) {
        this.variableMap.put(id + DynamicLength.ELEMENT_WIDTH_SUFFIX, width);
        this.variableMap.put(id + DynamicLength.ELEMENT_HEIGHT_SUFFIX, height);
    }

    public void putLength(String id, int length) {
        this.variableMap.put(id + DynamicLength.ELEMENT_LENGTH_SUFFIX, length);
    }

    protected Graphics2D createGraphics(BufferedImage image) {
        var g2d = image.createGraphics();
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2d.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);

        return g2d;
    }

    public Graphics2D getGraphics(int index) {
        if (this.graphicsList == null) throw new IllegalStateException("graphics list is not initialized");
        int i = index % this.length;
        var g2d = this.graphicsList.get(i);
        if (g2d == null) {
            var frame = this.frameList.get(i);
            synchronized (frame) {
                g2d = createGraphics(frame.image);
                this.graphicsList.set(i, g2d);
                return g2d;
            }
        }
        return g2d;
    }

    public void dispose() {
        graphicsList.forEach(Graphics2D::dispose);
    }

    public LengthContext createLengthContext(int elementWidth, int elementHeight) {
        var lengthContext = new LengthContext(this.width, this.height, elementWidth, elementHeight);
        lengthContext.variables = this.variableMap;
        return lengthContext;
    }

    public LengthContext getLengthContext() {
        if (staticLengthContext == null) {
            staticLengthContext = createLengthContext(0, 0);
        }
        return staticLengthContext;
    }

    protected static BufferedImage cloneImage(BufferedImage raw) {
        ColorModel cm = raw.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = raw.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}
