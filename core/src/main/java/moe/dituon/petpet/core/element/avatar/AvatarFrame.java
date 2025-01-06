package moe.dituon.petpet.core.element.avatar;

import moe.dituon.petpet.core.clip.BorderRadius;
import moe.dituon.petpet.core.element.ElementFrame;
import moe.dituon.petpet.core.filter.ImageFilterList;
import moe.dituon.petpet.core.position.FitType;
import moe.dituon.petpet.core.transform.Offset;
import moe.dituon.petpet.core.transition.RotateTransition;
import moe.dituon.petpet.core.utils.image.ImageCropper;
import moe.dituon.petpet.template.element.AvatarTemplate;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public abstract class AvatarFrame extends ElementFrame {
    public final AvatarTemplate template;

    public final String id;
    @Nullable
    public final String defaultUrl;
    public final ImageCropper cropper;
    public final FitType fit;
    public final Offset position;
    public final float angle;
    public final Offset origin;
    public final float opacity;
    public final AlphaComposite alphaComposite;
    @Nullable
    public final BorderRadius borderRadius;
    public final RotateTransition rotateTransition;
    public final ImageFilterList filterList;

    protected AvatarFrame(int index, AvatarTemplate template) {
        super.index = index;
        this.template = template;
        this.id = getNElement(template.getKey());
        this.defaultUrl = getNElement(template.getDefault());
        this.cropper = getNElement(template.getCrop());
        this.fit = getNElement(template.getFit());
        this.position = getNElement(template.getPosition());
        this.angle = getNElement(template.getAngle());
        this.origin = getNElement(template.getOrigin());
        this.opacity = getNElement(template.getOpacity());
        this.borderRadius = template.getBorderRadius().isEmpty() ? null : getNElement(template.getBorderRadius());
        this.alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
        this.rotateTransition = template.getRotate();
        this.filterList = template.getFilter();
    }

    /**
     * Get the expected width of the image; <br/>
     * Expected value to come from absolute coordinates.
     * @return 0 if coordinates are not absolute
     */
    public int getExpectedWidth() {
        return 0;
    }

    /**
     * Get the expected height of the image; <br/>
     * Expected value to come from absolute coordinates.
     * @return 0 if coordinates are not absolute
     */
    public int getExpectedHeight() {
        return 0;
    }
}
