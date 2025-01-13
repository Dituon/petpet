package moe.dituon.petpet.core.element;

import moe.dituon.petpet.core.context.RequestContext;
import moe.dituon.petpet.core.utils.image.EncodedImage;
import moe.dituon.petpet.template.Metadata;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface PetpetModel {
    default EncodedImage draw() {
        return draw(RequestContext.EMPTY);
    }

    EncodedImage draw(RequestContext requestContext);

    Metadata getMetadata();

    @Nullable
    default File getPreviewImage() {
        return null;
    }
}
