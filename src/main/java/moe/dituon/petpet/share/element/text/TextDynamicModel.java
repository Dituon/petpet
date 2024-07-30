package moe.dituon.petpet.share.element.text;

import moe.dituon.petpet.share.TextData;
import moe.dituon.petpet.share.template.TextExtraData;
import moe.dituon.petpet.share.element.FrameInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextDynamicModel extends TextModel {
    public static final Pattern TEXT_VAR_REGEX = Pattern.compile("\\$txt([1-9]\\d*)\\[(.*?)]"); //$txt(num)[(xxx)]

    protected String text;
    protected TextTemplate data;

    public TextDynamicModel(TextTemplate textData, TextExtraData extraInfo) {
        this.data = textData;
        text = extraInfo != null ? buildText(
                textData.getText(), extraInfo, textData.getGreedy()
        ) : textData.getText();
        paragraph = TextBuilder.buildParagraph(
                new GraphicsAttributedString(text, textData),
                textData,
                textData.getPos().length > 2 ? textData.getPos()[2] : DEFAULT_WIDTH
        );
    }

    protected String buildText(String text, TextExtraData extraData, boolean greedy) {
        for (var e : extraData.getMap().entrySet()) {
            text = text.replace('$' + e.getKey(), e.getValue());
        }

//        text = text.replace("$from", extraData.getFromReplacement())
//                .replace("$to", extraData.getToReplacement())
//                .replace("$group", extraData.getGroupReplacement());

        Matcher m = TEXT_VAR_REGEX.matcher(text);
        if (greedy) {
            List<String> textList = new ArrayList<>(extraData.getList());
            short maxIndex = 0;
            while (m.find()) maxIndex++;
            m.reset();
            for (short index = 0; m.find(); index++) {
                short i = Short.parseShort(m.group(1));
                String replaceText = i > textList.size() ?
                        m.group(2) : (index == maxIndex ?
                        textList.remove(i - 1) : String.join(" ", textList)
                );
                text = text.replace(m.group(0), replaceText);
            }
        } else {
            while (m.find()) {
                short i = Short.parseShort(m.group(1));
                String replaceText = i > extraData.getList().size() ?
                        m.group(2) : extraData.getList().get(i - 1);
                text = text.replace(m.group(0), replaceText);
            }
        }
        return text;
    }

    /**
     * 在Graphics2D对象上绘制TextModel
     *
     * @param g2d 画布
     */
    @Override
    public void draw(Graphics2D g2d, FrameInfo info) {
        var pos = data.getPos();
        paragraph.draw(g2d, pos[0], pos[1]);
    }
}

