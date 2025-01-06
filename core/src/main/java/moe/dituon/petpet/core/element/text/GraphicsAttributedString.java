package moe.dituon.petpet.core.element.text;

import lombok.Getter;
import moe.dituon.petpet.core.GlobalContext;
import moe.dituon.petpet.core.element.ElementFrame;
import moe.dituon.petpet.core.length.Length;
import moe.dituon.petpet.core.transform.Offset;
import moe.dituon.petpet.template.element.TextAlign;
import moe.dituon.petpet.template.element.TextBaseline;
import moe.dituon.petpet.template.element.TextTemplate;
import moe.dituon.petpet.template.element.TextWrap;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.List;
import java.util.Map;

@Getter
public class GraphicsAttributedString extends AttributedString {
    public final int index;
    public final String text;
    public final Color color;
    public final Font font;
    public final int maxFontSize;
    public final TextAlign align;
    public final TextBaseline baseline;
    public final TextWrap wrap;
    public final float angle;
    public final float theta;
    public final Offset origin;
    public final float strokeSize;
    public final Color strokeColor;
    public final Length x;
    public final Length y;
    public final Length width;

    private static Font getFont(TextTemplate data, int index) {
        return GlobalContext.getInstance().fontManager.getFont(
                ElementFrame.getNElement(data.getFontName(), index),
                ElementFrame.getNElement(data.getStyle(), index).getValue(),
                Math.round(ElementFrame.getNElement(data.getSize(), index))
        );
    }

    public GraphicsAttributedString(TextTemplate data, int index) {
        this(ElementFrame.getNElement(data.getText(), index), data, index);
    }

    public GraphicsAttributedString(String text, TextTemplate data, int index) {
        super(text, Map.of(TextAttribute.FONT, getFont(data, index)));
        this.index = index;
        this.text = text;
        this.font = getFont(data, index);
        this.maxFontSize = (int) getNElement(data.getMaxSize());
        this.color = getNElement(data.getColor());
        this.align = getNElement(data.getAlign());
        this.baseline = getNElement(data.getBaseline());
        this.wrap = getNElement(data.getWrap());
        this.origin = getNElement(data.getOrigin());
        this.angle = getNElement(data.getAngle());
        this.theta = (float) Math.toRadians(this.angle);
        this.strokeColor = getNElement(data.getStrokeColor());
        this.strokeSize = getNElement(data.getStrokeSize());

        var coords = getNElement(data.getCoords());
        this.x = coords.coordsList.get(0);
        this.y = coords.coordsList.get(1);
        if (coords.coordsList.size() > 2) {
            this.width = coords.coordsList.get(2);
        } else {
            this.width = TextModel.DEFAULT_WIDTH;
        }
    }

    public GraphicsAttributedString(String text, GraphicsAttributedString string) {
        super(text, Map.of(TextAttribute.FONT, string.font));
        this.index = string.index;
        this.text = text;
        this.font = string.font;
        this.maxFontSize = string.maxFontSize;
        this.color = string.color;
        this.align = string.align;
        this.baseline = string.baseline;
        this.wrap = string.wrap;
        this.origin = string.origin;
        this.angle = string.angle;
        this.theta = string.theta;
        this.strokeColor = string.strokeColor;
        this.strokeSize = string.strokeSize;
        this.x = string.x;
        this.y = string.y;
        this.width = string.width;
    }

    protected float getNElement(float[] array) {
        return array[index % array.length];
    }

    protected <T> T getNElement(List<T> list) {
        if (list == null) return null;
        return list.get(index % list.size());
    }
}
