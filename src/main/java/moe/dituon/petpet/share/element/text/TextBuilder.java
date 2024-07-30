package moe.dituon.petpet.share.element.text;

import lombok.Getter;
import moe.dituon.petpet.share.*;
import moe.dituon.petpet.share.template.ExtraData;
import moe.dituon.petpet.share.template.TextExtraData;

import java.util.List;

public class TextBuilder {
    public static final List<String> EXPR_LIST = List.of("$txt", "$from", "$to", "$group");

    @Getter
    protected final TextTemplate data;
    @Getter
    protected final boolean dynamic;
    protected GraphicsParagraph staticParagraph;

    public TextBuilder(TextTemplate data) {
        this.data = data;
        var text = data.getText();
        this.dynamic = EXPR_LIST.stream().anyMatch(text::contains);
        if (!dynamic) {
            staticParagraph = this.buildParagraph(
                    new GraphicsAttributedString(data),
                    data.getPos().length > 2 ? data.getPos()[2] : TextModel.DEFAULT_WIDTH
            );
        }
    }

    public GraphicsParagraph buildParagraph(GraphicsAttributedString string) {
        return buildParagraph(string, data);
    }

    public GraphicsParagraph buildParagraph(GraphicsAttributedString string, int width) {
        return buildParagraph(string, data, width);
    }

    public static GraphicsParagraph buildParagraph(GraphicsAttributedString string, TextTemplate data) {
        return buildParagraph(string, data.getWrap(), data.getAlign(), data.getBaseline());
    }

    public static GraphicsParagraph buildParagraph(GraphicsAttributedString string, TextTemplate data, int width) {
        return buildParagraph(string, data.getWrap(), data.getAlign(), data.getBaseline(), width);
    }

    public static GraphicsParagraph buildParagraph(
            GraphicsAttributedString string,
            TextWrap wrap,
            TextAlign align,
            TextBaseline baseline
    ) {
        switch (wrap) {
            case ZOOM:
                return new GraphicsZoomParagraph(string, align, baseline);
            case BREAK:
                return new GraphicsBreakParagraph(string, align, baseline);
            case NONE:
                return new GraphicsParagraph(string, align, baseline);
        }
        throw new RuntimeException();
    }

    public static GraphicsParagraph buildParagraph(
            GraphicsAttributedString string,
            TextWrap wrap,
            TextAlign align,
            TextBaseline baseline,
            int width
    ) {
        switch (wrap) {
            case ZOOM:
                return new GraphicsZoomParagraph(string, align, baseline, width);
            case BREAK:
                return new GraphicsBreakParagraph(string, align, baseline, width);
            case NONE:
                return new GraphicsParagraph(string, align, baseline);
        }
        throw new RuntimeException();
    }

    public TextModel build(ExtraData data) {
        return build(data.getText());
    }

    public TextModel build(TextExtraData extraData) {
        if (dynamic) {
            return new TextDynamicModel(data, extraData);
        } else {
            return new TextStaticModel(data, staticParagraph);
        }
    }
}
