package moe.dituon.petpet.share.element.avatar;

import lombok.Getter;
import lombok.NonNull;
import moe.dituon.petpet.share.AvatarPosType;
import moe.dituon.petpet.share.position.PositionCollection;
import moe.dituon.petpet.share.position.PositionCollectionFactory;
import moe.dituon.petpet.share.position.PositionP4ACollection;
import moe.dituon.petpet.share.position.PositionXYWHCollection;
import moe.dituon.petpet.share.service.ResourceManager;
import moe.dituon.petpet.share.template.ExtraData;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AvatarBuilder {
    protected final String type;
    @Getter
    protected final AvatarTemplate template;
    protected final AvatarPosType posType;
    protected final PositionCollection<?> pos;
    protected final Supplier<List<BufferedImage>> defaultImageGetter;

    public static final List<String> REPLACE_VALUES = List.of("from", "to", "group", "bot", "random");
    public static final List<String> REPLACE_KEYS = REPLACE_VALUES.stream().map(String::toUpperCase).collect(Collectors.toList());

    public AvatarBuilder(AvatarTemplate template) {
        this(template, null);
    }

    /**
     * @param localPath 用于处理本地图像的相对路径, Nullable
     */
    public AvatarBuilder(AvatarTemplate template, Path localPath) {
        posType = template.getPosType();
        pos = PositionCollectionFactory.createCollection(template.getPos(), posType);
        this.template = template;
        var index = REPLACE_KEYS.indexOf(template.getType());
        if (index != -1){
            this.type = REPLACE_VALUES.get(index);
        } else {
            this.type = template.getType();
        }

        var defaultImageUri = template.getDefault();
        if (defaultImageUri == null) {
            this.defaultImageGetter = null;
            return;
        }
        this.defaultImageGetter = ResourceManager.getDefaultInstance().getImageSupplier(defaultImageUri, localPath);
    }

    public AvatarModel build(ExtraData data) {
        var getter = data.getAvatar().getMap().get(type);

        if (getter == null) {
            getter = defaultImageGetter;
            if (getter == null) {
                throw new RuntimeException("Avatar " + type + " not found!");
            }
        } else if (defaultImageGetter != null) {
            var rawGetter = getter;
            getter = () -> {
                try {
                    return rawGetter.get();
                } catch (RuntimeException e) {
                    return defaultImageGetter.get();
                }
            };
        }

        return build(getter);
    }


    public AvatarModel build(@NonNull Supplier<List<BufferedImage>> supplier) {
        switch (posType) {
            case ZOOM:
                return new AvatarXYWHModel(template, supplier, (PositionXYWHCollection) pos);
            case DEFORM:
                return new AvatarDeformModel(template, supplier, (PositionP4ACollection) pos);
        }
        throw new RuntimeException();
    }
}
