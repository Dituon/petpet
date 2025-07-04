package moe.dituon.petpet.service;

import lombok.Getter;
import moe.dituon.petpet.core.FontManager;
import moe.dituon.petpet.core.GlobalContext;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.element.ElementModel;
import moe.dituon.petpet.core.element.PetpetModel;
import moe.dituon.petpet.core.element.PetpetTemplateModel;
import moe.dituon.petpet.core.element.avatar.AvatarModel;
import moe.dituon.petpet.core.utils.image.EncodedImage;
import moe.dituon.petpet.script.PetpetScriptModel;
import moe.dituon.petpet.template.PetpetTemplate;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.*;

public abstract class BaseService extends TemplateManger {
    public static final String VERSION = "1.0.0-beta3";
    protected static final Random RANDOM = new Random();
    protected static final FontManager FONT_MANAGER = GlobalContext.getInstance().fontManager;
    protected final Map<PetpetModel, Map<String, Integer>> templateExpectedSizeCache = new HashMap<>(256);

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

    public @Nullable String getTemplateId(String idOrAlias) {
        if (staticModelMap.containsKey(idOrAlias)) {
            return idOrAlias;
        }
        var ids = aliaIdMap.get(idOrAlias);
        if (ids != null) {
            return ids.get(RANDOM.nextInt(ids.size()));
        }
        return null;
    }

    public String[] getTemplateIds(String idOrAlias) {
        if (staticModelMap.containsKey(idOrAlias)) {
            return new String[]{idOrAlias};
        }
        var ids = aliaIdMap.get(idOrAlias);
        if (ids != null) {
            return ids.toArray(String[]::new);
        }
        return new String[0];
    }

    public @Nullable PetpetModel getTemplateById(String id) {
        return staticModelMap.get(id);
    }

    @Override
    public @Nullable PetpetModel getTemplate(String idOrAlias) {
        return staticModelMap.get(getTemplateId(idOrAlias));
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

    public Map<String, Integer> getTemplateExpectedSize(PetpetModel model) {
        return templateExpectedSizeCache.computeIfAbsent(model, k -> {
            var map = new HashMap<String, Integer>(4);
            if (model instanceof PetpetScriptModel) {
                return Collections.emptyMap();
            }
            for (ElementModel ele : ((PetpetTemplateModel) model).getElementList()) {
                if (ele.getElementType() != ElementModel.Type.AVATAR) {
                    continue;
                }
                var avatarEle = (AvatarModel) ele;
                int size = Math.max(avatarEle.getExpectedWidth(), avatarEle.getExpectedHeight());
                for (String key : avatarEle.template.getKey()) {
                    map.merge(key, size, Math::max);
                }
            }
            return map;
        });
    }

    @Override
    public void clear() {
        aliaIdMap.clear();
        templateExpectedSizeCache.clear();
        super.clear();
    }
}
