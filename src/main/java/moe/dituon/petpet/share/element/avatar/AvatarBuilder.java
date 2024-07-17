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
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AvatarBuilder {
    @Getter
    private final AvatarTemplate template;
    private final AvatarPosType posType;
    private final PositionCollection<?> pos;
    protected final Supplier<List<BufferedImage>> defaultImageGetter;

    public static final List<String> REPLACE_KEYS = List.of("from", "to", "group", "bot", "random");
    public static final List<String> REPLACE_VALUES = REPLACE_KEYS.stream().map(String::toUpperCase).collect(Collectors.toList());

    public AvatarBuilder(AvatarTemplate template) {
        this(template, null);
    }

    /**
     * @param localPath 用于处理本地图像的相对路径
     */
    public AvatarBuilder(AvatarTemplate template, Path localPath) {
        posType = template.getPosType();
        pos = PositionCollectionFactory.createCollection(template.getPos(), posType);
        this.template = template;

        var defaultImageUri = template.getDefault();
        if (defaultImageUri == null) {
            this.defaultImageGetter = null;
            return;
        }
        System.out.println(localPath);
        if (localPath != null && defaultImageUri.getScheme().equals("file") && !isAbsolutePath(defaultImageUri)) {
            System.out.println(localPath);
            defaultImageUri = localPath.resolve(defaultImageUri.getSchemeSpecificPart()).toUri();
        }
        final URI finalDefaultImageUri = defaultImageUri;
        this.defaultImageGetter = () -> {
            try {
                return List.of(ResourceManager.getDefaultInstance().getImages(
                        finalDefaultImageUri
                ));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public AvatarModel build(ExtraData data) {
        String type = this.template.getType();

        var getter = data.getAvatar().get(type);
        if (getter == null) {
            int index = REPLACE_KEYS.indexOf(type);
            if (index != -1) getter = data.getAvatar().get(REPLACE_VALUES.get(index));

            if (getter == null && defaultImageGetter != null) {
                getter = defaultImageGetter;
            }

            if (getter == null) {
                throw new RuntimeException("Avatar " + type + " not found!");
            }
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

    protected static boolean isAbsolutePath(URI uri) {
        if (!uri.isOpaque()) {
            return uri.getPath().startsWith("/");
        }
        return false;
    }
}
