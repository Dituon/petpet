package moe.dituon.petpet.core.filter;

import lombok.experimental.Delegate;
import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.template.element.AvatarTemplate;
import moe.dituon.petpet.template.fields.ImageFilterTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

public class ImageFilterList implements List<ImageFilterTemplate> {
    @Delegate
    public final List<ImageFilterTemplate> filterList;
    public static final ImageFilterList EMPTY = new EmptyFilterList();

    public ImageFilterList(List<ImageFilterTemplate> filterList) {
        this.filterList = filterList;
    }

    public BufferedImage filter(@NotNull BufferedImage image, int i) {
        return filter(image, i, null, null);
    }

    public BufferedImage filter(
            @NotNull BufferedImage image,
            int i,
            @Nullable AvatarTemplate parentTemplate,
            @Nullable RequestContext requestContext
    ) {
        for (ImageFilterTemplate filter : this.filterList) {
            image = filter.filter(image, i, parentTemplate, requestContext);
        }
        return image;
    }

    private static class EmptyFilterList extends ImageFilterList {
        public EmptyFilterList() {
            super(Collections.emptyList());
        }

        @Override
        public BufferedImage filter(@NotNull BufferedImage image, int i) {
            return image;
        }

        @Override
        public BufferedImage filter(
                @NotNull BufferedImage image,
                int i,
                @Nullable AvatarTemplate parentTemplate,
                @Nullable RequestContext requestContext
        ) {
            return image;
        }
    }
}
