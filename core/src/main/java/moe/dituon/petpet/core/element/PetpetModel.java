package moe.dituon.petpet.core.element;

import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.utils.image.EncodedImage;
import moe.dituon.petpet.template.Metadata;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.Map;

public interface PetpetModel {
    default EncodedImage draw() {
        return draw(RequestContext.newImmutableEmpty());
    }

    EncodedImage draw(RequestContext requestContext);

    Metadata getMetadata();

    void setMetadata(Metadata metadata);

    @Nullable
    default File getPreviewImage() {
        return null;
    }

    /**
     * 获取模板工作目录
     */
    @Nullable
    default File getDirectory() {
        return null;
    }

    /**
     * 获取模板资源hash
     */
    default Map<String, String> getResourceMD5Map() {
        return Collections.emptyMap();
    }
}
