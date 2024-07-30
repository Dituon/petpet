package moe.dituon.petpet.share.element.text;

import lombok.Getter;
import moe.dituon.petpet.share.*;

import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Map;

@Getter
public class GraphicsAttributedString extends AttributedString {
    protected String text;
    protected Font font;
    protected Color color;
    protected TextAlign align;
    protected TextBaseline baseline;
    protected TextWrap wrap;
    protected int angle;
    protected TransformOrigin origin;
    protected int strokeSize;
    protected Color strokeColor;

    public static Map<? extends AttributedCharacterIterator.Attribute, ?> asAttributeMap(TextTemplate data) {
        Font font = new Font(data.getFont(), data.getStyle().getValue(), data.getSize());
        return Map.of(TextAttribute.FONT, font);
    }

    public GraphicsAttributedString(TextTemplate data) {
        this(data.getText(), data);
    }

    public GraphicsAttributedString(String text, TextTemplate data) {
        super(text, asAttributeMap(data));
        this.text = text;
        this.font = StyleContext.getDefaultStyleContext().getFont(
                data.getFont(), data.getStyle().getValue(), data.getSize()
        );
        this.color = data.getAwtColor();
        this.align = data.getAlign();
        //TODO
//        this.baseline = data.getBaseline();
        this.baseline = TextBaseline.TOP;
        this.wrap = data.getWrap();
        this.origin = data.getOrigin();
        this.angle = data.getAngle();
        this.strokeColor = data.getStrokeAwtColor();
        this.strokeSize = data.getStrokeSize();
    }
}
