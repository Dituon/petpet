package moe.dituon.petpet.share.template.background;

import kotlinx.serialization.json.JsonElement;
import kotlinx.serialization.json.JsonPrimitive;
import moe.dituon.petpet.share.ArithmeticParser;
import moe.dituon.petpet.share.BackgroundData;
import moe.dituon.petpet.share.element.TemplateElement;
import moe.dituon.petpet.share.element.avatar.AvatarModel;
import moe.dituon.petpet.share.element.text.TextModel;
import moe.dituon.petpet.share.service.BackgroundResource;

import java.util.regex.Pattern;

public class BackgroundFactory {
    public static final Pattern EXPR_REGEX = Pattern.compile("(avatar|text)(\\d+)(Height|Width)");

    protected final BackgroundResource resource;
    protected final BackgroundData data;
    protected int width;
    protected int height;

    protected String widthParser;
    protected String heightParser;

    protected BackgroundModel staticModel;

    public BackgroundFactory(BackgroundResource resource, BackgroundData data) {
        this.resource = resource;
        this.data = data;

        if (data == null) {
            this.staticModel = new BackgroundModel(resource, null, 0, 0);
            return;
        }

        boolean dynamic = EXPR_REGEX.matcher(data.getSize().toString()).find();

        if (!dynamic) {
            int[] size = new int[data.getSize().size()];
            int i = 0;
            for (JsonElement je : data.getSize()) {
                size[i++] = Integer.parseInt(((JsonPrimitive) je).getContent());
            }
            this.width = size[0];
            this.height = size[1];

            this.staticModel = new BackgroundModel(resource, data, width, height);
        } else {
            // width
            String str = ((JsonPrimitive) data.getSize().get(0)).getContent();
            try {
                this.width = Integer.parseInt(str);
            } catch (NumberFormatException ignored) {
                this.widthParser = str;
            }
            // height
            str = ((JsonPrimitive) data.getSize().get(1)).getContent();
            try {
                this.height = Integer.parseInt(str);
            } catch (NumberFormatException ignored) {
                this.heightParser = str;
            }
        }
    }

    public BackgroundModel build(
            Iterable<AvatarModel> avatars, Iterable<TextModel> texts
    ) {
        if (staticModel != null) {
            return staticModel;
        }

        int w = width;
        if (widthParser != null) {
            var wp = new ArithmeticParser(widthParser);
            putParserSize(wp, avatars);
            putParserSize(wp, texts);
            w = (int) wp.eval();
        }
        int h = height;
        if (heightParser != null) {
            var hp = new ArithmeticParser(heightParser);
            putParserSize(hp, avatars);
            putParserSize(hp, texts);
            h = (int) hp.eval();
        }

        return new BackgroundModel(this.resource, this.data, w, h);
    }

    public void putParserSize(ArithmeticParser parser, Iterable<? extends TemplateElement> elements) {
        int avatarIndex = 0;
        int textIndex = 0;
        for (TemplateElement element : elements) {
            if (element.getElementType() == TemplateElement.Type.AVATAR) {
                parser.put("avatar" + avatarIndex + "Width", element.getWidth());
                parser.put("avatar" + avatarIndex + "Height", element.getHeight());
                avatarIndex++;
            } else if (element.getElementType() == TemplateElement.Type.TEXT) {
                parser.put("text" + textIndex + "Width", element.getWidth());
                parser.put("text" + textIndex + "Height", element.getHeight());
                textIndex++;
            }
        }
    }
}
