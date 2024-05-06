package moe.dituon.petpet.share.element.text;

import lombok.Getter;
import moe.dituon.petpet.share.*;

import java.util.List;

public class TextFactory {
    public static final List<String> EXPR_LIST = List.of("$txt", "$from", "$to", "$group");

    @Getter
    protected final TextData data;
    @Getter
    protected final boolean dynamic;
    protected GraphicsParagraph staticParagraph;

    public TextFactory(TextData data) {
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

    public static GraphicsParagraph buildParagraph(GraphicsAttributedString string, TextData data) {
        return buildParagraph(string, data.getWrap(), data.getAlign(), data.getBaseline());
    }

    public static GraphicsParagraph buildParagraph(GraphicsAttributedString string, TextData data, int width) {
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

    public TextModel build(TextExtraData extraData) {
        if (dynamic) {
            return new TextDynamicModel(data, extraData);
        } else {
            return new TextStaticModel(data, staticParagraph);
        }
    }
}
