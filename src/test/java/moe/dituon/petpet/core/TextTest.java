package moe.dituon.petpet.core;

import moe.dituon.petpet.share.*;
import moe.dituon.petpet.share.element.text.TextTemplate;
import moe.dituon.petpet.share.template.TextExtraData;
import moe.dituon.petpet.share.element.FrameInfo;
import moe.dituon.petpet.share.element.text.TextBuilder;
import moe.dituon.petpet.share.element.text.TextModel;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TextTest {
    public static final String outputDir = ".test-output/text/";
    public static final TextAlign[] testAligns = TextAlign.values();
    public static final TextBaseline[] testBaselines = new TextBaseline[]{
            TextBaseline.TOP,
            TextBaseline.MIDDLE,
            TextBaseline.BOTTOM
    };

    public static BufferedImage getTestImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
    }

    public static BufferedImage testTextModel(TextTemplate template) {
        return testTextModel(template, -1);
    }

    public static BufferedImage testTextModel(TextTemplate template, int width) {
        var img = getTestImage(width > 0 ? width : TextModel.DEFAULT_WIDTH, 200);

        switch (template.getAlign()) {
            case CENTER:
                template.getPos()[0] = img.getWidth() / 2;
                break;
            case RIGHT:
                template.getPos()[0] = img.getWidth();
                break;
        }
        switch (template.getBaseline()) {
            case MIDDLE:
                template.getPos()[1] = img.getHeight() / 2;
                break;
            case BOTTOM:
                template.getPos()[1] = img.getHeight();
                break;
        }

        if (width > 0) {
            var pos = template.getPos();
            template.setPos(new int[]{pos[0], pos[1], width});
        }

        var g2d = img.createGraphics();
        g2d.setColor(Color.RED);
        g2d.drawRect(0, 0, img.getWidth() - 1, img.getHeight() - 1);

        g2d.setColor(Color.BLUE);
        g2d.drawLine(template.getPos()[0], 0, template.getPos()[0], img.getHeight());
        g2d.drawLine(0, template.getPos()[1], img.getWidth(), template.getPos()[1]);
        g2d.fillRect(template.getPos()[0] - 5, template.getPos()[1] - 5, 10, 10);

        var builder = new TextBuilder(template);
        var model = builder.build(TestUtils.getTextExtraData());
        model.draw(g2d, new FrameInfo(0, img.getWidth(), img.getHeight()));
        return img;
    }

    public static BufferedImage createImageGroup(List<BufferedImage> images) {
        int col = 3;
        int width = images.get(0).getWidth() * col;
        int height = images.get(0).getHeight() * (images.size() / col);
        var img = new BufferedImage(width, height, images.get(0).getType());
        var g2d = img.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        for (int i = 0; i < images.size(); i++) {
            g2d.drawImage(images.get(i), i % col * images.get(0).getWidth(), i / col * images.get(0).getHeight(), null);
        }
        return img;
    }

    public static void saveImage(BufferedImage image, String name) throws IOException {
        var path = outputDir + name + ".png";
        var file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
            file.createNewFile();
        }
        ImageIO.write(image, "png", file);
    }

    @Test
    public void testWrapNone() throws IOException {
        var list = new ArrayList<BufferedImage>();
        for (TextAlign align : testAligns) {
            for (TextBaseline baseline : testBaselines) {
                var template = new TextTemplate(
                        "TextWrap.NONE\n段落测试\n" +
                                String.format("(align = %s, baseline = %s)", align, baseline)
                );
                template.setWrap(TextWrap.NONE);
                template.setAlign(align);
                template.setBaseline(baseline);
                list.add(testTextModel(template));
            }
        }
        var img = createImageGroup(list);
        saveImage(img, "wrapNone");
    }

    @Test
    public void testWrapBreak() throws IOException {
        var list = new ArrayList<BufferedImage>();
        for (TextAlign align : testAligns) {
            for (TextBaseline baseline : testBaselines) {
                var template = new TextData(
                        "TextWrap.BREAK\n段落测试\n" + String.format("(align = %s, baseline = %s)", align, baseline)
                );
                template.setWrap(TextWrap.BREAK);
                template.setAlign(align);
                template.setBaseline(baseline);
                list.add(testTextModel(template));
            }
        }
        var img = createImageGroup(list);
        saveImage(img, "wrapBreak");
    }

    @Test
    public void testWrapZoom() throws IOException {
        var list = new ArrayList<BufferedImage>();
        for (TextAlign align : testAligns) {
            for (TextBaseline baseline : testBaselines) {
                var template = new TextData(
                        "TextWrap.ZOOM\n段落测试\n" + String.format("(align = %s, baseline = %s)", align, baseline)
                );
                template.setWrap(TextWrap.ZOOM);
                template.setAlign(align);
                template.setBaseline(baseline);
                list.add(testTextModel(template));
            }
        }
        var img = createImageGroup(list);
        saveImage(img, "wrapZoom");
    }
}
