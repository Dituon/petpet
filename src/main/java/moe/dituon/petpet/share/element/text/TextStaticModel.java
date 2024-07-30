package moe.dituon.petpet.share.element.text;

import moe.dituon.petpet.share.TextData;
import moe.dituon.petpet.share.element.FrameInfo;

import java.awt.*;

public class TextStaticModel extends TextModel {
    protected int x;
    protected int y;
    protected final GraphicsParagraph paragraph;

    public TextStaticModel(TextTemplate textData, GraphicsParagraph paragraph) {
        var pos = textData.getPos();
        x = pos[0];
        y = pos[1];
        this.paragraph = paragraph;
    }

    @Override
    public void draw(Graphics2D g2d, FrameInfo info) {
        paragraph.draw(g2d, x, y);
    }
}
