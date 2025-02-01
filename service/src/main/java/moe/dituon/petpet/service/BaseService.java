package moe.dituon.petpet.service;

import lombok.Getter;
import moe.dituon.petpet.core.FontManager;
import moe.dituon.petpet.core.GlobalContext;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.element.PetpetModel;
import moe.dituon.petpet.core.utils.image.EncodedImage;
import moe.dituon.petpet.template.PetpetTemplate;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.*;

public abstract class BaseService extends TemplateManger {
    public static final String VERSION = "1.0.0-beta2";
    protected static final Random RANDOM = new Random();
    protected static final FontManager FONT_MANAGER = GlobalContext.getInstance().fontManager;

    @Getter
    protected Map<String, List<String>> aliaIdMap = new HashMap<>(512);

    @Override
    public PetpetModel addTemplate(String id, PetpetModel template) {
        var prev = super.addTemplate(id, template);
        for (String alia : template.getMetadata().getAlias()) {
            this.aliaIdMap.computeIfAbsent(alia, k -> new ArrayList<>(4)).add(id);
        }
        return prev;
    }

    @Override
    public @Nullable PetpetModel getTemplate(String idOrAlias) {
        var template = staticModelMap.get(idOrAlias);
        if (template == null) {
            var ids = aliaIdMap.get(idOrAlias);
            if (ids == null) return null;
            template = staticModelMap.get(ids.get(RANDOM.nextInt(ids.size())));
        }
        return template;
    }

    @Override
    public PetpetModel removeTemplate(String id) {
        var template = super.removeTemplate(id);
        if (template == null) return null;
        var alias = template.getMetadata().getAlias();
        for (String alia : alias) {
            this.aliaIdMap.computeIfPresent(alia, (key, list) -> {
                list.remove(id);
                return list.isEmpty() ? null : list;
            });
        }
        return template;
    }

    public EncodedImage generate(String idOrAlias, RequestContext requestContext) {
        var model = this.getTemplate(idOrAlias);
        if (model == null) throw new IllegalArgumentException("Template not found: " + idOrAlias);
        return model.draw(requestContext);
    }

    public boolean addFont(File fontFile) throws IOException, FontFormatException {
        return FONT_MANAGER.addFont(fontFile);
    }

    public void addFonts(Path fontDirectory) {
        FONT_MANAGER.addFonts(fontDirectory);
    }

    public String setDefaultFontFamily(String defaultFamily) {
        return FONT_MANAGER.setDefaultFontFamily(defaultFamily);
    }
}
