package moe.dituon.petpet.core.element.text;

import lombok.Getter;
import moe.dituon.petpet.core.GlobalContext;
import moe.dituon.petpet.core.context.CanvasContext;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.element.ElementModel;
import moe.dituon.petpet.core.length.Length;
import moe.dituon.petpet.core.length.LengthType;
import moe.dituon.petpet.core.length.NumberLength;
import moe.dituon.petpet.core.position.TextXYWCoords;
import moe.dituon.petpet.template.element.TextTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TextModel implements ElementModel {
    public static final Length DEFAULT_WIDTH = new NumberLength(200, LengthType.PX);
    public static final Color DEFAULT_COLOR = Color.decode("#191919");
    public static final Color DEFAULT_STROKE_COLOR = Color.WHITE;

    protected final TextTemplate template;
    protected final List<GraphicsParagraph> paragraphList;
    @Getter
    public final boolean isAbsolute;

    public TextModel(TextTemplate template) {
        this(template, true);
    }

    protected TextModel(TextTemplate template, boolean initFlag) {
        this.template = template;
        this.paragraphList = new ArrayList<>(template.getMaxLength());
        this.isAbsolute = template.getCoords().stream().allMatch(TextXYWCoords::isAbsolute);
        if (initFlag) init(template);
    }

    protected TextModel(TextDynamicModel dynamicModel) {
        this.template = dynamicModel.template;
        this.paragraphList = dynamicModel.paragraphList;
        this.isAbsolute = dynamicModel.isAbsolute;
        init(template);
    }

    protected void init(TextTemplate template) {
        for (int i = 0; i < template.getMaxLength(); i++) {
            var string = new GraphicsAttributedString(template, i);
            this.paragraphList.add(createParagraph(string));
        }
    }

    public boolean isDynamic() {
        return false;
    }

    @Override
    public @Nullable String getId() {
        return this.template.getId();
    }

    @Override
    public Type getElementType() {
        return Type.TEXT;
    }

    public static GraphicsParagraph createParagraph(
            GraphicsAttributedString string
    ) {
        switch (string.wrap) {
            case ZOOM:
                return new GraphicsZoomParagraph(string);
            case BREAK:
                return new GraphicsBreakParagraph(string);
            case NONE:
                return new GraphicsParagraph(string);
        }
        throw new UnknownError();
    }

    public static GraphicsParagraph createParagraph(
            GraphicsAttributedString string,
            int width
    ) {
        switch (string.wrap) {
            case ZOOM:
                return new GraphicsZoomParagraph(string, width);
            case BREAK:
                return new GraphicsBreakParagraph(string, width);
            case NONE:
                return new GraphicsParagraph(string);
        }
        throw new UnknownError();
    }

    protected static List<GraphicsParagraph> repeatByLength(@NotNull List<GraphicsParagraph> list, int length) {
        if (list.size() >= length) return list;
        if (list.size() == 1) return Collections.nCopies(length, list.get(0));
        return IntStream.range(0, length)
                .mapToObj(i -> list.get(i % list.size()))
                .collect(Collectors.toList());
    }

    @Override
    public void draw(CanvasContext canvasContext, RequestContext requestContext) {
        int canvasLength = canvasContext.getLength();
        var frameList = paragraphList;
        if (paragraphList.size() <= canvasLength) {
            frameList = repeatByLength(frameList, canvasLength);
        }
        GlobalContext.getInstance().execImageProcess(frameList, (i, p) -> {
            p.draw(
                    canvasContext.getGraphics(i),
                    canvasContext.createLengthContext(p.width, p.height)
            );
        });
    }

    @Override
    public RenderedElement render(CanvasContext canvasContext, RequestContext requestContext) {
        return new RenderedElement(canvasContext, requestContext);
    }

    public class RenderedElement extends ElementModel.RenderedElement {
        @Getter
        protected int width;
        @Getter
        protected int height;
        protected final CanvasContext canvasContext;
        protected final RequestContext requestContext;

        public RenderedElement(CanvasContext canvasContext, RequestContext requestContext) {
            this.width = paragraphList.isEmpty() ? 0
                    : paragraphList.stream().mapToInt(p -> p.width).max().orElse(0);
            this.height = paragraphList.isEmpty() ? 0
                    : paragraphList.stream().mapToInt(p -> p.height).max().orElse(0);
            this.canvasContext = canvasContext;
            this.requestContext = requestContext;
        }

        @Override
        public void draw() {
            TextModel.this.draw(this.canvasContext, this.requestContext);
        }

        @Override
        public int getLength() {
            return template.getMaxLength();
        }
    }

    public static TextModel createTextModel(TextTemplate template) {
        var dynamicModel = new TextDynamicModel(template);
        if (dynamicModel.isDynamic) {
            return dynamicModel;
        }
        return new TextModel(dynamicModel);
    }

    public static TextModel fromDynamicModel(TextDynamicModel dynamicModel) {
        if (dynamicModel.isDynamic) {
            throw new IllegalArgumentException("Dynamic model cannot be converted to static model");
        }
        return new TextModel(dynamicModel);
    }
}
