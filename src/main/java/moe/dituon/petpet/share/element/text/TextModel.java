package moe.dituon.petpet.share.element.text;

import moe.dituon.petpet.share.element.TemplateElement;

import java.awt.*;

public abstract class TextModel implements TemplateElement {
    public static final int DEFAULT_WIDTH = 200;
    public static final String DEFAULT_COLOR_STR = "#191919";
    public static final Color DEFAULT_COLOR = Color.decode(DEFAULT_COLOR_STR);
    public static final String DEFAULT_STROKE_COLOR_STR = "#fffff";
    public static final Color DEFAULT_STROKE_COLOR = Color.decode(DEFAULT_STROKE_COLOR_STR);

    protected GraphicsParagraph paragraph;

    @Override
    public TemplateElement.Type getElementType() {
        return TemplateElement.Type.TEXT;
    }

    @Override
    public int getWidth() {
        return paragraph.width;
    }

    @Override
    public int getHeight() {
        return paragraph.height;
    }
}
